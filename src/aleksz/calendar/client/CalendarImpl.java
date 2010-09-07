package aleksz.calendar.client;

import static com.google.gwt.dom.client.NativeEvent.BUTTON_RIGHT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aleksz.holidays.client.PublicHoliday;
import aleksz.utils.client.Coordinate;
import aleksz.utils.client.Date;
import aleksz.utils.client.DateRange;
import aleksz.utils.client.StringUtils;
import aleksz.utils.client.timer.Timer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;



/**
 *
 * @author aleksz
 *
 */
public class CalendarImpl implements Calendar {

  public interface View {
    void setDayFromOtherMonth(int row, int col, int day);
    void setDay(int row, int col, int day);
    void select(int row, int col);
    void deselect(int row, int col);
    void markToday(int row, int col);
    void markEvent(int row, int col, String label);
    void unmarkEvent(int row, int col, String label);
    void markPublicHoliday(int row, int col, String label);
    void hover(int row, int col);
    void dehover(int row, int col);
    int getCurrentMonth();
    void setCurrentMonth(int month);
    int getCurrentYear();
    void setCurrentYear(int year);
    int getFirstDayOfWeek();
    Set<PublicHoliday> getPublicHolidays();

    HasMouseWheelHandlers getMouseWheelSource();
    HasClickHandlers getDayAsClickSource(int row, int col);
    HasMouseOverHandlers getDayAsMouseOverSource(int row, int col);
    HasMouseOutHandlers getDayAsMouseOutSource(int row, int col);
    HasMouseDownHandlers getDayAsMouseDownSource(int row, int col);
    HasMouseUpHandlers getDayAsMouseUpSource(int row, int col);
    HasClickHandlers getPrevMonthButton();
    HasClickHandlers getNextMonthButton();
    HasChangeHandlers getMonthSelect();
    HasChangeHandlers getYearsSelect();
    HasAllMouseHandlers getGridWrapper();
    HandlerRegistration addValueChangeHandler(ValueChangeHandler<DateRange> handler);
    Timer getDragToNextMonthTimer();
    void fireEvent(GwtEvent<?> event);

    Widget asWidget();
  }

  public static final int ROWS = 6;
  public static final int COLS = 7;
  public static final int DRAG_MONTH_SWITCH_DELAY_MS = 1000;

  private CalendarModel model;
  private View view;

  public CalendarImpl() {
    this(new CalendarView());
  }

  public CalendarImpl(DateRange selectedRange) {
    this(selectedRange, new CalendarView());
  }

  public CalendarImpl(Date selectedDate) {
    this(new DateRange(selectedDate, selectedDate), new CalendarView());
  }

  public CalendarImpl(View view) {
    this.model = new CalendarModel();
    this.view = view;
    init();
  }

  public CalendarImpl(Date selectedDate, View view) {
    this(new DateRange(selectedDate, selectedDate), view);
  }

  public CalendarImpl(DateRange selectedRange, View view) {
    this.model = new CalendarModel(selectedRange);
    this.view = view;
    init();
  }

  private void init() {
    addNavigationButtonHandlers();
    addNavigationSelectHandlers();
    addMouseWheelHandlers();
    addGridWrapperHandlers();
    drawGrid();
  }

  @Override
  public void prevMonth() {
    model.setToPrevMonth();
    drawGrid();
  }

  @Override
  public void nextMonth() {
    model.setToNextMonth();
    drawGrid();
  }

  @Override
  public void prevYear() {
    model.currentYear--;
    drawGrid();
  }

  @Override
  public void nextYear() {
    model.currentYear++;
    drawGrid();
  }

  @Override
  public void setValue(DateRange range) {

    int monthBeforeUpdate = model.currentMonth;
    model.value = range;

    if (range != null && monthBeforeUpdate == model.currentMonth) {
      selectRangeOnView(range);
    } else {
      drawGrid();
    }
  }

  @Override
  public void unmarkEvent(DateRange range, String label) {
    for (Date d : range) {
      List<String> eventsOnThisDate = model.events.get(d);
      if (eventsOnThisDate == null) { continue; }
      if (!eventsOnThisDate.remove(label)) { continue; }
      unmarkEventOnView(d, label);
    }
  }

  @Override
  public void markEvent(DateRange range, String label) {
    for (Date date : range) {
      putEventToModel(date, label);
      markEventOnView(date, label);
    }
  }

  @Override
  public void markEvent(Date date, String label) {
    putEventToModel(date, label);
    markEventOnView(date, label);
  }

  private void putEventToModel(Date date, String label) {
    List<String> eventsThisDay = model.events.get(date);
    if (eventsThisDay == null) {
      eventsThisDay = new ArrayList<String>();
      model.events.put(date, eventsThisDay);
    }
    eventsThisDay.add(label);
  }

  private void drawGrid() {
    Date firstDayOfMonth = new Date(model.currentYear, model.currentMonth, 1);
    Date lastDayOfMonth = firstDayOfMonth.lastDayOfMonth();
    int daysFromPrevMonth = firstDayOfMonth.getDayOfWeek() + 1 - view.getFirstDayOfWeek();
    Date dateToDraw = firstDayOfMonth.substractDays(daysFromPrevMonth);

    model.dateToCoordinate.clear();

    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {

        model.dateToCoordinate.put(dateToDraw, new Coordinate(row, col));

        if (dateToDraw.before(firstDayOfMonth) || dateToDraw.after(lastDayOfMonth)) {
          view.setDayFromOtherMonth(row, col, dateToDraw.getDay());
        } else {
          view.setDay(row, col, dateToDraw.getDay());
        }

        markAsTodayIfReallyToday(dateToDraw, row, col);
        markAsSelectedIfReallySelected(dateToDraw, row, col);
        addClickHandler(dateToDraw, row, col);
        addHoverHandlers(dateToDraw, row, col);
        addSelectionHandlers(dateToDraw, row, col);

        dateToDraw = dateToDraw.nextDay();
      }
    }

    markEventsOnView();
    markPublicHolidaysOnView();
    view.setCurrentMonth(model.currentMonth);
    view.setCurrentYear(model.currentYear);
  }

  private void addSelectionHandlers(final Date dateToDraw, final int row, final int col) {
    view.getDayAsMouseDownSource(row, col).addMouseDownHandler(new MouseDownHandler() {

      @Override
      public void onMouseDown(MouseDownEvent event) {

        if (BUTTON_RIGHT == event.getNativeButton()) {
          return;
        }

        clearSelectionOnView();
        view.select(row, col);
        model.dragStart = dateToDraw;
      }
    });

    view.getDayAsMouseUpSource(row, col).addMouseUpHandler(new MouseUpHandler() {

      @Override
      public void onMouseUp(MouseUpEvent event) {

        Date newValueStart = model.dragStart;
        model.resetDrag();

        if (!newValueStart.equals(dateToDraw)) {
          setValue(new DateRange(newValueStart, dateToDraw), true);
        }
      }
    });
  }

  private void clearSelectionOnView() {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        view.deselect(row, col);
      }
    }
  }

  private void selectRangeOnView(DateRange range) {
    clearSelectionOnView();

    Date current = range.getFrom();
    while (!current.after(range.getTo())) {
      Coordinate c = model.dateToCoordinate.get(current);
      if (c != null) {
        view.select(c.x, c.y);
      }
      current = current.nextDay();
    }
  }

  private void addNavigationSelectHandlers() {
    view.getMonthSelect().addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        if (model.currentMonth == view.getCurrentMonth()) {
          return;
        }

        model.currentMonth = view.getCurrentMonth();
        drawGrid();
      }
    });

    view.getYearsSelect().addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        if (model.currentYear == view.getCurrentMonth()) {
          return;
        }

        model.currentYear = view.getCurrentYear();
        drawGrid();
      }
    });
  }

  private void addGridWrapperHandlers() {
    view.getGridWrapper().addMouseOutHandler(new MouseOutHandler() {

      @Override
      public void onMouseOut(MouseOutEvent event) {
        if (model.dragStart != null) {
          clearSelectionOnView();
          model.resetDrag();
        }
      }
    });
  }

  private void addMouseWheelHandlers() {
    view.getMouseWheelSource().addMouseWheelHandler(new MouseWheelHandler() {

      @Override
      public void onMouseWheel(MouseWheelEvent event) {
        if (event.isNorth()) {
          nextMonth();
        } else {
          prevMonth();
        }
      }
    });
  }

  private void addNavigationButtonHandlers() {
    view.getPrevMonthButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        prevMonth();
      }
    });

    view.getNextMonthButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        nextMonth();
      }
    });
  }

  private void addClickHandler(final Date dateToDraw, final int row, final int col) {
    view.getDayAsClickSource(row, col).addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        NativeEvent e = event.getNativeEvent();
        if (GWT.isClient() && (e.getShiftKey() || e.getCtrlKey()) && model.value != null) {
          if (dateToDraw.before(model.value.getFrom())) {
            setValue(new DateRange(dateToDraw, model.value.getTo()), true);
          } else if (dateToDraw.after(model.value.getFrom())) {
            setValue(new DateRange(model.value.getFrom(), dateToDraw), true);
          }
        } else {
          setValue(new DateRange(dateToDraw), true);
        }
        view.dehover(row, col);
      }
    });
  }

  private void addHoverHandlers(final Date dateToDraw, final int row, final int col) {

    view.getDayAsMouseOutSource(row, col).addMouseOutHandler(new MouseOutHandler() {

      @Override
      public void onMouseOut(MouseOutEvent event) {
        view.dehover(row, col);
      }

    });

    view.getDayAsMouseOverSource(row, col).addMouseOverHandler(new MouseOverHandler() {

      @Override
      public void onMouseOver(MouseOverEvent event) {

        if (model.dragStart == null) {
          view.hover(row, col);
          return;
        }

        model.dragEnd = dateToDraw;

        if (isLastCell(row, col)) {
          view.getDragToNextMonthTimer().schedule(
              new DragToNextMonthTask(dateToDraw),
              DRAG_MONTH_SWITCH_DELAY_MS);

        } else if (isFirstCell(row, col)) {
          view.getDragToNextMonthTimer().schedule(
              new DragToPrevMonthTask(dateToDraw),
              DRAG_MONTH_SWITCH_DELAY_MS);
        }

        selectRangeOnView(new DateRange(model.dragStart, dateToDraw));
      }

    });
  }

  private boolean isFirstCell(int row, int col) {
    return row == 0 && col == 0;
  }

  private boolean isLastCell(final int row, final int col) {
    return row == (ROWS - 1) && col == (COLS - 1);
  }

  private Coordinate getLastCell() {
    return new Coordinate(ROWS - 1, COLS - 1);
  }

  private void markAsSelectedIfReallySelected(Date dateToDraw, int row, int col) {

    view.deselect(row, col);

    if (model.value != null && model.value.isInRange(dateToDraw)) {
      view.select(row, col);
    }
  }

  private void markAsTodayIfReallyToday(Date dateToDraw, int row, int col) {
    if (model.today.equals(dateToDraw)) {
      view.markToday(row, col);
    }
  }

  private void markEventOnView(Date date, String label) {
    Coordinate c = model.dateToCoordinate.get(date);
    if (c == null) { return; }
    view.markEvent(c.x, c.y, label);
  }

  private void unmarkEventOnView(Date date, String label) {
    Coordinate c = model.dateToCoordinate.get(date);
    if (c == null) { return; }
    view.unmarkEvent(c.x, c.y, label);
  }

  private void markPublicHolidaysOnView() {
    for (PublicHoliday holiday : view.getPublicHolidays()) {
      markHolidayOnView(holiday, model.currentYear);
      if (model.currentMonth == 12) {
        markHolidayOnView(holiday, model.currentYear + 1);
      }
      if (model.currentMonth == 1) {
        markHolidayOnView(holiday, model.currentYear - 1);
      }
    }
  }

  private void markHolidayOnView(PublicHoliday holiday, int year) {

    DateRange range = holiday.getHolidayForYear(year);

    if (range == null) { return; }

    for (Date d : range) {
      Coordinate c = model.dateToCoordinate.get(d);
      if (c == null) { continue; }
      String label = holiday.getName();
      if (!holiday.isGlobal()) {
        label += "(" + StringUtils.join(",", holiday.getCountryCodes()) + ")";
      }
      view.markPublicHoliday(c.x, c.y, label);
    }
  }

  private void markEventsOnView() {
    for (Map.Entry<Date, Coordinate> e : model.dateToCoordinate.entrySet()) {
      List<String> labelsForDate = model.events.get(e.getKey());
      if (labelsForDate == null) { continue; }
      Coordinate c = e.getValue();
      for (String label : labelsForDate) {
        view.markEvent(c.x, c.y, label);
      }
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public DateRange getValue() {
    return model.value;
  }

  @Override
  public void setValue(DateRange value, boolean fireEvents) {
    DateRange prevValue = model.value;
    setValue(value);
    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, prevValue, value);
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<DateRange> handler) {
    return view.addValueChangeHandler(handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    view.fireEvent(event);
  }

  private final class DragToNextMonthTask implements Runnable {

    private Date dateThatFired;

    private DragToNextMonthTask(Date dateThatFired) {
      this.dateThatFired = dateThatFired;
    }

    @Override
    public void run() {

      if (dateThatFired.equals(model.dragEnd)) {
        nextMonth();
        Date lastDateInView = model.getDateByCoordinate(getLastCell());
        selectRangeOnView(new DateRange(model.dragStart, lastDateInView));
      }
    }
  }

  private final class DragToPrevMonthTask implements Runnable {

    private Date dateThatFired;

    private DragToPrevMonthTask(Date dateThatFired) {
      this.dateThatFired = dateThatFired;
    }

    @Override
    public void run() {

      if (dateThatFired.equals(model.dragEnd)) {
        prevMonth();
        selectRangeOnView(new DateRange(model.dragStart, model.dragEnd));
      }
    }
  }
}

class CalendarModel {

  DateRange value;
  Date today = new Date();
  int currentYear;
  int currentMonth;
  Date dragStart;
  Date dragEnd;
  Map<Date, List<String>> events = new HashMap<Date, List<String>>();
  Map<Date, Coordinate> dateToCoordinate = new HashMap<Date, Coordinate>();

  public CalendarModel() {
    setToToday();
  }

  public CalendarModel(DateRange value) {
    this.value = value;
    setToValueStart();
  }

  public void resetDrag() {
    dragEnd = null;
    dragStart = null;
  }

  public void setToPrevMonth() {
    currentMonth--;
    if (currentMonth < 1) {
      currentMonth = 12;
      currentYear--;
    }
  }

  public void setToNextMonth() {
    currentMonth++;
    if (currentMonth > 12) {
      currentMonth = 1;
      currentYear++;
    }
  }

  public void setToToday() {
    currentMonth = today.getMonth();
    currentYear = today.getYear();
  }

  public void setToValueStart() {
    currentMonth = value.getFrom().getMonth();
    currentYear = value.getTo().getYear();
  }

  public Date getDateByCoordinate(Coordinate c) {
    for (Map.Entry<Date, Coordinate> e : dateToCoordinate.entrySet()) {
      if (e.getValue().equals(c)) {
        return e.getKey();
      }
    }

    return null;
  }
}