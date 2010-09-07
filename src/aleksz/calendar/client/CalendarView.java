package aleksz.calendar.client;

import static aleksz.calendar.client.CalendarImpl.COLS;
import static aleksz.calendar.client.CalendarImpl.ROWS;
import static aleksz.utils.client.StringUtils.isEmpty;
import static com.google.gwt.user.client.ui.DockPanel.CENTER;
import static com.google.gwt.user.client.ui.DockPanel.EAST;
import static com.google.gwt.user.client.ui.DockPanel.WEST;

import java.util.List;
import java.util.Set;

import aleksz.calendar.client.i18n.CalendarMessages;
import aleksz.holidays.client.PublicHoliday;
import aleksz.holidays.client.PublicHolidaysFactory;
import aleksz.utils.client.BrowserUtils;
import aleksz.utils.client.Date;
import aleksz.utils.client.DateRange;
import aleksz.utils.client.DateTimeConstatUtils;
import aleksz.utils.client.timer.Timer;
import aleksz.utils.client.timer.TimerWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.DateTimeConstants;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author aleksz
 *
 */
public class CalendarView extends FocusPanel implements CalendarImpl.View {

  private static CalendarMessages messages = GWT.create(CalendarMessages.class);
  private static DateTimeConstants constants =
    LocaleInfo.getCurrentLocale().getDateTimeConstants();
  private static final int YEARS_IN_SELECT = 6;

  private DockPanel outer;
  private DockPanel navBar;
  private Grid grid;
  private Grid headerGrid;
  private FocusPanel gridWrapper;
  private Label prevMonth;
  private Label nextMonth;
  private ListBox monthsDropDown;
  private ListBox yearsDropDown;
  private TimerWidget dragToNextMonthTimer = new TimerWidget();

  public CalendarView() {
    add(getOuter());
    setStyleName("CalendarWidget");
  }

  private ListBox getYearsDropDown() {
    if (yearsDropDown != null) {
      return yearsDropDown;
    }

    yearsDropDown = new ListBox();
    yearsDropDown.setStyleName("navSelect");

    Date today = new Date();
    for (int i = YEARS_IN_SELECT; i >= 0; i--) {
      String year = String.valueOf(today.getYear() - i);
      yearsDropDown.addItem(year);
    }

    for (int i = 1; i < YEARS_IN_SELECT; i++) {
      String year = String.valueOf(today.getYear() + i);
      yearsDropDown.addItem(year);
    }

    return yearsDropDown;
  }

  private ListBox getMonthsDropDown() {
    if (monthsDropDown != null) {
      return monthsDropDown;
    }

    monthsDropDown = new ListBox();
    monthsDropDown.setStyleName("navSelect");

    for (int i = 0; i < constants.shortMonths().length; i++) {
      monthsDropDown.addItem(constants.shortMonths()[i], String.valueOf(i + 1));
    }

    return monthsDropDown;
  }

  private DockPanel getOuter() {
    if (outer != null) {
      return outer;
    }

    outer = new DockPanel();
    outer.add(getNavBar(), DockPanel.NORTH);
    outer.add(getHeaderGrid(), DockPanel.NORTH);
    outer.add(getGridWrapper(), DockPanel.CENTER);

    return outer;
  }

  private Label getNextMonth() {
    if (nextMonth != null) {
      return nextMonth;
    }

    nextMonth = new Label(">");
    nextMonth.setStyleName("monthButton");

    return nextMonth;
  }

  private Label getPrevMonth() {
    if (prevMonth != null) {
      return prevMonth;
    }

    prevMonth = new Label("<");
    prevMonth.setStyleName("monthButton");

    return prevMonth;
  }

  private DockPanel getNavBar() {
    if (navBar != null) {
      return navBar;
    }

    FlowPanel center = new FlowPanel();
    center.add(getMonthsDropDown());
    center.add(getYearsDropDown());
    center.setStyleName("navCenter");
    navBar = new DockPanel();
    navBar.add(center, CENTER);
    navBar.add(getPrevMonth(), WEST);
    navBar.add(getNextMonth(), EAST);
    navBar.setStyleName("navBar");

    return navBar;
  }

  public FocusPanel getGridWrapper() {
    if (gridWrapper != null) {
      return gridWrapper;
    }

    gridWrapper = new FocusPanel();
    gridWrapper.add(getGrid());

    return gridWrapper;
  }

  private Grid getHeaderGrid() {
    if (headerGrid != null) {
      return headerGrid;
    }

    headerGrid = new Grid(1, COLS);
    headerGrid.setStyleName("table");
    headerGrid.setCellSpacing(0);
    headerGrid.getRowFormatter().setStyleName(0, "weekheader");

    String[] dayNames = DateTimeConstatUtils.getWeekDays(
        constants.shortWeekdays(), constants.firstDayOfTheWeek());

    for (int i = 0; i < dayNames.length; i++) {
      headerGrid.getCellFormatter().setStyleName(0, i, "days");
      headerGrid.setText(0, i, dayNames[i]);
    }

    return headerGrid;
  }

  private Grid getGrid() {
    if (grid != null) {
      return grid;
    }

    grid = new Grid(ROWS, COLS);
    grid.setStyleName("table");
    grid.setCellSpacing(0);
    BrowserUtils.disableTextSelect(grid.getElement());

    List<Integer> weekEndIndexes =
      DateTimeConstatUtils.convertWeekEndRangeToIndexes(
          constants.weekendRange(),
          constants.firstDayOfTheWeek(),
          constants.shortWeekdays().length);

    for (int row = 0; row < ROWS; row++) {
      for (int col : weekEndIndexes) {
        grid.getCellFormatter().addStyleName(row, col, "weekend");
      }
    }

    return grid;
  }

  @Override
  public void markEvent(int row, int col, String label) {
    getCellWidget(row, col).addStyleName("event");
    addLabel(row, col, label);
  }

  @Override
  public void markPublicHoliday(int row, int col, String label) {
    getCellWidget(row, col).addStyleName("public");
    addLabel(row, col, label);
  }

  private void addLabel(int row, int col, String label) {
    Label l = getCellWidget(row, col);
    label = "*" + label;
    if (!isEmpty(l.getTitle())) {
      label = l.getTitle() + "\n" + label;
    }
    l.setTitle(label);
  }

  @Override
  public void setDay(int row, int col, int day) {
    Label dayInGrid = new Label(String.valueOf(day));
    dayInGrid.setStyleName("dayInGrid");
    getGrid().setWidget(row, col, dayInGrid);
  }

  @Override
  public void setDayFromOtherMonth(int row, int col, int day) {
    setDay(row, col, day);
    getCellWidget(row, col).addStyleName("dayFromOtherMonth");
  }

  @Override
  public void select(int row, int col) {
    getCellWidget(row, col).addStyleName("selected");
  }

  @Override
  public void markToday(int row, int col) {
    getCellWidget(row, col).addStyleName("today");
    addLabel(row, col, messages.today());
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void deselect(int row, int col) {
    getCellWidget(row, col).removeStyleName("selected");
  }

  @Override
  public HasClickHandlers getDayAsClickSource(int row, int col) {
    return getCellWidget(row, col);
  }

  @Override
  public HasMouseOutHandlers getDayAsMouseOutSource(int row, int col) {
    return getCellWidget(row, col);
  }

  @Override
  public HasMouseOverHandlers getDayAsMouseOverSource(int row, int col) {
    return getCellWidget(row, col);
  }

  @Override
  public HasMouseDownHandlers getDayAsMouseDownSource(int row, int col) {
    return getCellWidget(row, col);
  }

  @Override
  public HasMouseUpHandlers getDayAsMouseUpSource(int row, int col) {
    return getCellWidget(row, col);
  }

  @Override
  public void dehover(int row, int col) {
    getCellWidget(row, col).removeStyleName("hover");
  }

  @Override
  public void hover(int row, int col) {
    getCellWidget(row, col).addStyleName("hover");
  }

  private Label getCellWidget(int row, int col) {
    return (Label) getGrid().getWidget(row, col);
  }

  @Override
  public HasClickHandlers getNextMonthButton() {
    return getNextMonth();
  }

  @Override
  public HasClickHandlers getPrevMonthButton() {
    return getPrevMonth();
  }

  @Override
  public int getCurrentMonth() {
    return new Integer(getMonthsDropDown().getValue(getMonthsDropDown().getSelectedIndex()));
  }

  @Override
  public HasChangeHandlers getMonthSelect() {
    return getMonthsDropDown();
  }

  @Override
  public void setCurrentMonth(int month) {
    getMonthsDropDown().setSelectedIndex(month - 1);
  }

  @Override
  public int getCurrentYear() {
    return new Integer(getYearsDropDown().getValue(getYearsDropDown().getSelectedIndex()));
  }

  @Override
  public HasChangeHandlers getYearsSelect() {
    return getYearsDropDown();
  }

  @Override
  public void setCurrentYear(int year) {
    int firstYearInDropDown = new Date().getYear() - YEARS_IN_SELECT;
    getYearsDropDown().setSelectedIndex(year - firstYearInDropDown);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<DateRange> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public HasMouseWheelHandlers getMouseWheelSource() {
    return this;
  }

  @Override
  public int getFirstDayOfWeek() {
    return new Integer(constants.firstDayOfTheWeek());
  }

  @Override
  public void unmarkEvent(int row, int col, String label) {
    Label l = getCellWidget(row, col);
    String[] parts = l.getTitle().split("\n");
    String newValue = "";
    for (String part : parts) {
      if (!part.equals("*" + label)) {
        newValue += part + "\n";
      }
    }

    l.setTitle(newValue);

    if (newValue.length() == 0) {
      l.removeStyleName("event");
    }
  }

  @Override
  public Set<PublicHoliday> getPublicHolidays() {
    return PublicHolidaysFactory.getAllPublicHolidays();
  }

  @Override
  public Timer getDragToNextMonthTimer() {
    return dragToNextMonthTimer;
  }
}