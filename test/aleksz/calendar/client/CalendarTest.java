package aleksz.calendar.client;

import static aleksz.calendar.client.CalendarImpl.COLS;
import static aleksz.calendar.client.CalendarImpl.DRAG_MONTH_SWITCH_DELAY_MS;
import static aleksz.calendar.client.CalendarImpl.ROWS;
import static com.google.gwt.dom.client.NativeEvent.BUTTON_LEFT;
import static com.google.gwt.dom.client.NativeEvent.BUTTON_RIGHT;
import static java.util.Arrays.deepEquals;
import static java.util.Arrays.deepToString;
import static java.util.Arrays.fill;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import aleksz.holidays.client.PublicHoliday;
import aleksz.holidays.client.StaticPublicHoliday;
import aleksz.utils.client.Date;
import aleksz.utils.client.DateRange;
import aleksz.utils.client.timer.MockTimer;
import aleksz.utils.client.timer.Timer;
import aleksz.utils.mock.MockChangeEvent;
import aleksz.utils.mock.MockClickEvent;
import aleksz.utils.mock.MockHasAllMouseEvents;
import aleksz.utils.mock.MockHasChangeHandlers;
import aleksz.utils.mock.MockHasClickHandlers;
import aleksz.utils.mock.MockHasMouseDownHandlers;
import aleksz.utils.mock.MockHasMouseOutHandlers;
import aleksz.utils.mock.MockHasMouseOverHandlers;
import aleksz.utils.mock.MockHasMouseUpHandlers;
import aleksz.utils.mock.MockHasMouseWheelHandlers;
import aleksz.utils.mock.MockMouseDownEvent;
import aleksz.utils.mock.MockMouseOutEvent;
import aleksz.utils.mock.MockMouseOverEvent;
import aleksz.utils.mock.MockMouseUpEvent;
import aleksz.utils.mock.MockMouseWheelEvent;
import aleksz.utils.mock.MockValueChangeHandler;

import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
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
public class CalendarTest {

  private MockView view;

  @Before
  public void init() {
    view = new MockView();
    view.firstDayOfWeek = 2;
  }

  @Test
  public void dragResetOnMouseOutFromGrid() {
    new CalendarImpl(view);

    view.dayDownHandlers[0][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.gridWrapperHandlers.out.lastClickHandler.onMouseOut(new MockMouseOutEvent());

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void rangeSelectedWhenMonthSwitchedWithDragAndDragEndHovered() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(0);
    view.dayOverHandlers[4][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[3][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void rangeSelectedWhenMonthSwitchedWithDragAndDragEndSelected() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(0);
    view.dayOverHandlers[4][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[3][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayUpHandlers[3][6].lastClickHandler.onMouseUp(new MockMouseUpEvent());

    boolean[][] expected = new boolean[][] {
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void rangeSelectedWhenMonthSwitchedWithDragToFirstDayInView() {
    new CalendarImpl(new Date(2009, 12, 22), view);

    view.dayDownHandlers[0][1].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {true, true, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    view.dragToNextMonthTimer.runTask(0);
    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void rangeSelectedWhenMonthSwitchedWithDragToLastDayInView() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true},
        {true, true, true, true, true, true, true}
    };

    view.dragToNextMonthTimer.runTask(0);
    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void noMonthSwitchOnHoverToLastDayInCurrentView() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    assertEquals(0, view.dragToNextMonthTimer.getScheduledTasksLength());
    assertEquals(new Integer(5), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void noMonthSwitchOnHoverToFirstDayInCurrentView() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    assertEquals(0, view.dragToNextMonthTimer.getScheduledTasksLength());
    assertEquals(new Integer(5), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void noMonthSwitchOnDragToLastDayIfMouseUpAfterScheduling() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayUpHandlers[5][6].lastClickHandler.onMouseUp(new MockMouseUpEvent());

    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(5), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void noMonthSwitchOnDragToFirstDayIfMouseUpAfterScheduling() {
    new CalendarImpl(new Date(2009, 12, 22), view);

    view.dayDownHandlers[0][1].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayUpHandlers[0][0].lastClickHandler.onMouseUp(new MockMouseUpEvent());

    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(12), view.currentMonth);
    assertEquals(new Integer(2009), view.currentYear);
  }

  @Test
  public void noMonthSwitchOnDragToLastDayIfMouseRemovedAfterScheduling() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(5), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void noMonthSwitchOnDragToFirstDayIfMouseRemovedAfterScheduling() {
    new CalendarImpl(new Date(2009, 12, 22), view);

    view.dayDownHandlers[0][1].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[0][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(12), view.currentMonth);
    assertEquals(new Integer(2009), view.currentYear);
  }

  @Test
  public void monthSwitchOnDragToLastDayInCurrentView() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(0));
    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(6), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void monthSwitchOnDragToFirstDayInCurrentView() {
    new CalendarImpl(new Date(2009, 12, 22), view);

    view.dayDownHandlers[0][1].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(0));
    view.dragToNextMonthTimer.runTask(0);
    assertEquals(new Integer(11), view.currentMonth);
    assertEquals(new Integer(2009), view.currentYear);
  }

  @Test
  public void monthSwitchOnDragToFirstDayInCurrentViewKeepsSwitching() {
    new CalendarImpl(new Date(2009, 12, 22), view);

    view.dayDownHandlers[0][1].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(0);
    view.dayOverHandlers[0][0].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(1);

    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(0));
    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(1));
    assertEquals(new Integer(10), view.currentMonth);
    assertEquals(new Integer(2009), view.currentYear);
  }

  @Test
  public void monthSwitchOnDragToLastDayInCurrentViewKeepsSwitching() {
    new CalendarImpl(new Date(2010, 5, 31), view);

    view.dayDownHandlers[5][0].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[5][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(0);
    view.dayOverHandlers[5][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dragToNextMonthTimer.runTask(1);

    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(0));
    assertEquals(new Long(DRAG_MONTH_SWITCH_DELAY_MS), view.dragToNextMonthTimer.getDelayMillis(1));
    assertEquals(new Integer(7), view.currentMonth);
    assertEquals(new Integer(2010), view.currentYear);
  }

  @Test
  public void testRightMouseDown() {
    new CalendarImpl(new Date(2009, 9, 19), view);
    view.dayDownHandlers[2][3].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_RIGHT));

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, true, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void testMouseDown() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    view.dayDownHandlers[2][3].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, true, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void testDragForwardAndBack() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    view.dayDownHandlers[2][3].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[2][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[2][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[2][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, true, true, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void testDragForwardSelection() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    view.dayDownHandlers[2][3].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[2][4].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[2][5].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, true, true, true, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void testDragBackwardSelection() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    view.dayDownHandlers[2][3].lastClickHandler.onMouseDown(new MockMouseDownEvent(BUTTON_LEFT));
    view.dayOverHandlers[2][2].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    view.dayOverHandlers[2][1].lastClickHandler.onMouseOver(new MockMouseOverEvent());

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, true, true, true, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void testHover() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    view.dayOverHandlers[4][6].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    assertEquals(new Integer(4), view.hoverRow);
    assertEquals(new Integer(6), view.hoverCol);

    view.dayOverHandlers[2][3].lastClickHandler.onMouseOver(new MockMouseOverEvent());
    assertEquals(new Integer(2), view.hoverRow);
    assertEquals(new Integer(3), view.hoverCol);
  }

  @Test
  public void markSelectedValues() {
    new CalendarImpl(new DateRange(new Date(2009, 9, 19), new Date(2009, 9, 20)), view);

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, true, true},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void populateMonthView() {
    new CalendarImpl(new Date(2009, 9, 19), view);

    Integer[][] expected = new Integer[][] {
        {31, 1, 2, 3, 4, 5, 6},
        {7, 8, 9, 10, 11, 12, 13},
        {14, 15, 16, 17, 18, 19, 20},
        {21, 22, 23, 24, 25, 26, 27},
        {28, 29, 30, 1, 2, 3, 4},
        {5, 6, 7, 8, 9, 10, 11}
    };

    assertTrue(deepToString(view.days), deepEquals(expected, view.days));
  }

  @Test
  public void populateMonthViewWithAnotherStartDay() {
    view.firstDayOfWeek = 1;
    new CalendarImpl(new Date(2009, 9, 19), view);

    Integer[][] expected = new Integer[][] {
        {30, 31, 1, 2, 3, 4, 5},
        {6, 7, 8, 9, 10, 11, 12},
        {13, 14, 15, 16, 17, 18, 19},
        {20, 21, 22, 23, 24, 25, 26},
        {27, 28, 29, 30, 1, 2, 3},
        {4, 5, 6, 7, 8, 9, 10}
    };

    assertTrue(deepToString(view.days), deepEquals(expected, view.days));
  }

  @Test
  public void todayIsMarked() {
    Date today = new Date();
    new CalendarImpl(today, view);
    int row = 0;
    int col = 0;
    for (row = 0; row < ROWS; row++) {
      for (col = 0; col < COLS; col++) {
        if (view.days[row][col] != null && view.days[row][col] == today.getDay()) {
          assertEquals(new Integer(row), view.todayRow);
          assertEquals(new Integer(col), view.todayCol);
          return;
        }
      }
    }
  }

  @Test
  public void unmarkEvents() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 17), view);
    String label = "testLabel";
    DateRange e1Range = new DateRange(new Date(2009, 9, 18), new Date(2009, 9, 19));
    c.markEvent(e1Range, label);
    String anotherLabel = "anotherTestLabel";
    c.markEvent(new Date(2009, 9, 19), anotherLabel);

    c.unmarkEvent(e1Range, label);
    assertNull("Event not unmarked", view.events[2][4][0]);
    assertNull("Event not unmarked", view.events[2][5][0]);
    assertEquals(anotherLabel, view.events[2][5][1]);
  }

  @Test
  public void markEventWithDate() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    String label = "testLabel";
    c.markEvent(new Date(2009, 9, 18), label);

    assertEquals(label, view.events[2][4][0]);
  }

  @Test
  public void markEventWithRange() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    String label = "testLabel";
    c.markEvent(new DateRange(new Date(2009, 9, 18), new Date(2009, 9, 21)), label);

    assertEquals(label, view.events[2][4][0]);
    assertEquals(label, view.events[2][5][0]);
    assertEquals(label, view.events[2][6][0]);
    assertEquals(label, view.events[3][0][0]);
  }

  @Test
  public void markEventWithOverlappingDate() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    String label1 = "testLabel1";
    String label2 = "testLabel2";
    c.markEvent(new Date(2009, 9, 18), label1);
    c.markEvent(new Date(2009, 9, 18), label2);

    assertEquals(label1, view.events[2][4][0]);
    assertEquals(label2, view.events[2][4][1]);
  }

  @Test
  public void markEventWithOverlappingRange() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    String label1 = "testLabel1";
    String label2 = "testLabel2";
    c.markEvent(new DateRange(new Date(2009, 9, 18), new Date(2009, 9, 19)), label1);
    c.markEvent(new DateRange(new Date(2009, 9, 19), new Date(2009, 9, 20)), label2);

    assertEquals(label1, view.events[2][4][0]);
    assertEquals(label1, view.events[2][5][0]);
    assertEquals(label2, view.events[2][5][1]);
    assertEquals(label2, view.events[2][6][0]);
  }

  @Test
  public void nextMonth() {
    String event = "TestLabel";
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    c.markEvent(new Date(2009, 10, 10), event);
    c.nextMonth();

    boolean[][] expectedSelection = new boolean[ROWS][COLS];
    for (int i = 0; i < expectedSelection.length; i++) {
      fill(expectedSelection[i], false);
    }

    Integer[][] expectedDays = new Integer[][] {
        {28, 29, 30, 1, 2, 3, 4},
        {5, 6, 7, 8, 9, 10, 11},
        {12, 13, 14, 15, 16, 17, 18},
        {19, 20, 21, 22, 23, 24, 25},
        {26, 27, 28, 29, 30, 31, 1},
        {2, 3, 4, 5, 6, 7, 8}
    };

    assertEquals(event, view.events[1][5][0]);
    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
    assertTrue(deepToString(view.selectedDays), deepEquals(expectedSelection, view.selectedDays));
  }

  @Test
  public void nextMonthSwitchesYear() {
    Calendar c = new CalendarImpl(new Date(2009, 12, 19), view);
    c.nextMonth();

    Integer[][] expectedDays = new Integer[][] {
        {28, 29, 30, 31, 1, 2, 3},
        {4, 5, 6, 7, 8, 9, 10},
        {11, 12, 13, 14, 15, 16, 17},
        {18, 19, 20, 21, 22, 23, 24},
        {25, 26, 27, 28, 29, 30, 31},
        {1, 2, 3, 4, 5, 6, 7}
    };

    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
  }

  @Test
  public void prevMonth() {
    String event = "TestLabel";
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    c.markEvent(new Date(2009, 8, 8), event);
    c.prevMonth();

    boolean[][] expectedSelection = new boolean[ROWS][COLS];
    for (int i = 0; i < expectedSelection.length; i++) {
      fill(expectedSelection[i], false);
    }

    Integer[][] expectedDays = new Integer[][] {
        {27, 28, 29, 30, 31, 1, 2},
        {3, 4, 5, 6, 7, 8, 9},
        {10, 11, 12, 13, 14, 15, 16},
        {17, 18, 19, 20, 21, 22, 23},
        {24, 25, 26, 27, 28, 29, 30},
        {31, 1, 2, 3, 4, 5, 6}
    };

    assertEquals(event, view.events[1][5][0]);
    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
    assertTrue(deepToString(view.selectedDays), deepEquals(expectedSelection, view.selectedDays));
  }

  @Test
  public void prevMonthSwitchesYear() {
    Calendar c = new CalendarImpl(new Date(2009, 1, 19), view);
    c.prevMonth();

    Integer[][] expectedDays = new Integer[][] {
        {1, 2, 3, 4, 5, 6, 7},
        {8, 9, 10, 11, 12, 13, 14},
        {15, 16, 17, 18, 19, 20, 21},
        {22, 23, 24, 25, 26, 27, 28},
        {29, 30, 31, 1, 2, 3, 4},
        {5, 6, 7, 8, 9, 10, 11}
    };

    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
  }

  @Test
  public void wheelNorth() {
    new CalendarImpl(new Date(2009, 9, 19), view);
    view.wheelHandler.lastClickHandler.onMouseWheel(new MockMouseWheelEvent(
        MockMouseWheelEvent.Direction.NORTH));

    Integer[][] expectedDays = new Integer[][] {
        {28, 29, 30, 1, 2, 3, 4},
        {5, 6, 7, 8, 9, 10, 11},
        {12, 13, 14, 15, 16, 17, 18},
        {19, 20, 21, 22, 23, 24, 25},
        {26, 27, 28, 29, 30, 31, 1},
        {2, 3, 4, 5, 6, 7, 8}
    };

    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
  }

  @Test
  public void wheelSouth() {
    new CalendarImpl(new Date(2009, 9, 19), view);
    view.wheelHandler.lastClickHandler.onMouseWheel(new MockMouseWheelEvent(
        MockMouseWheelEvent.Direction.SOUTH));

    Integer[][] expectedDays = new Integer[][] {
        {27, 28, 29, 30, 31, 1, 2},
        {3, 4, 5, 6, 7, 8, 9},
        {10, 11, 12, 13, 14, 15, 16},
        {17, 18, 19, 20, 21, 22, 23},
        {24, 25, 26, 27, 28, 29, 30},
        {31, 1, 2, 3, 4, 5, 6}
    };

    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
  }

  @Test
  public void testSelectMonth() {
    String event = "TestLabel";
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    c.markEvent(new Date(2009, 8, 8), event);
    view.currentMonth = 8;
    view.monthSelectHandler.lastClickHandler.onChange(new MockChangeEvent());

    boolean[][] expectedSelection = new boolean[ROWS][COLS];
    for (int i = 0; i < expectedSelection.length; i++) {
      fill(expectedSelection[i], false);
    }

    Integer[][] expectedDays = new Integer[][] {
        {27, 28, 29, 30, 31, 1, 2},
        {3, 4, 5, 6, 7, 8, 9},
        {10, 11, 12, 13, 14, 15, 16},
        {17, 18, 19, 20, 21, 22, 23},
        {24, 25, 26, 27, 28, 29, 30},
        {31, 1, 2, 3, 4, 5, 6}
    };

    assertEquals(event, view.events[1][5][0]);
    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
    assertTrue(deepToString(view.selectedDays), deepEquals(expectedSelection, view.selectedDays));
  }

  @Test
  public void testSelectYear() {
    String event = "TestLabel";
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    c.markEvent(new Date(2010, 9, 19), event);
    view.currentYear = 2010;
    view.yearSelectHandler.lastClickHandler.onChange(new MockChangeEvent());

    boolean[][] expectedSelection = new boolean[ROWS][COLS];
    for (int i = 0; i < expectedSelection.length; i++) {
      fill(expectedSelection[i], false);
    }

    Integer[][] expectedDays = new Integer[][] {
        {30, 31, 1, 2, 3, 4, 5},
        {6, 7, 8, 9, 10, 11, 12},
        {13, 14, 15, 16, 17, 18, 19},
        {20, 21, 22, 23, 24, 25, 26},
        {27, 28, 29, 30, 1, 2, 3},
        {4, 5, 6, 7, 8, 9, 10}
    };

    assertTrue(deepToString(view.days), deepEquals(expectedDays, view.days));
    assertTrue(deepToString(view.selectedDays), deepEquals(expectedSelection, view.selectedDays));
    assertEquals(event, view.events[2][6][0]);
  }

  @Test
  public void firesValueChangeOnDayClick() {
    Calendar c = new CalendarImpl(new Date(2009, 9, 19), view);
    MockValueChangeHandler<DateRange> handler = new MockValueChangeHandler<DateRange>();
    c.addValueChangeHandler(handler);
    view.dayClickHandlers[0][0].lastClickHandler.onClick(new MockClickEvent());

    assertEquals(new DateRange(new Date(2009, 8, 31)), handler.value);
  }

  @Test
  public void setNullAsValue() {
    Calendar c = new CalendarImpl(view);
    c.setValue(null);

    boolean[][] expected = new boolean[][] {
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false},
        {false, false, false, false, false, false, false}
    };

    assertTrue(deepToString(view.selectedDays), deepEquals(expected, view.selectedDays));
  }

  @Test
  public void publicSeveralHolidaysMarkedSameDay() {
    view.holidays.add(new StaticPublicHoliday("testHoliday", 9, 15));
    view.holidays.add(new StaticPublicHoliday("anotherTestHoliday", 9, 15));
    new CalendarImpl(new Date(2009, 9, 1), view);

    String[][] expected = new String[][] {
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, "anotherTestHoliday,testHoliday", null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null}
    };

    assertTrue(deepToString(view.markedHolidays), deepEquals(expected, view.markedHolidays));
  }

  @Test
  public void publicHolidaysMarked() {
    view.holidays.add(new StaticPublicHoliday("testHoliday", 9, 15));
    new CalendarImpl(new Date(2009, 9, 1), view);

    String[][] expected = new String[][] {
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, "testHoliday", null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null}
    };

    assertTrue(deepToString(view.markedHolidays), deepEquals(expected, view.markedHolidays));
  }

  @Test
  public void publicHolidaysMarkedForNextYear() {
    view.holidays.add(new StaticPublicHoliday("testHoliday", 1, 1));
    new CalendarImpl(new Date(2009, 12, 1), view);

    String[][] expected = new String[][] {
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, "testHoliday", null, null},
        {null, null, null, null, null, null, null}
    };

    assertTrue(deepToString(view.markedHolidays), deepEquals(expected, view.markedHolidays));
  }

  @Test
  public void publicHolidaysMarkedForPrevYear() {
    view.holidays.add(new StaticPublicHoliday("testHoliday", 12, 31));
    new CalendarImpl(new Date(2009, 1, 1), view);

    String[][] expected = new String[][] {
        {null, null, "testHoliday", null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null},
        {null, null, null, null, null, null, null}
    };

    assertTrue(deepToString(view.markedHolidays), deepEquals(expected, view.markedHolidays));
  }
}

class MockView implements CalendarImpl.View {

  String[][][] events = new String[ROWS][COLS][2];
  Integer[][] days = new Integer[ROWS][COLS];
  boolean[][] selectedDays = new boolean[ROWS][COLS];
  String[][] markedHolidays = new String[ROWS][COLS];
  Set<PublicHoliday> holidays = new HashSet<PublicHoliday>();
  MockHasClickHandlers[][] dayClickHandlers = new MockHasClickHandlers[ROWS][COLS];
  MockHasMouseOverHandlers[][] dayOverHandlers = new MockHasMouseOverHandlers[ROWS][COLS];
  MockHasMouseOutHandlers[][] dayOutHandlers = new MockHasMouseOutHandlers[ROWS][COLS];
  MockHasMouseDownHandlers[][] dayDownHandlers = new MockHasMouseDownHandlers[ROWS][COLS];
  MockHasMouseUpHandlers[][] dayUpHandlers = new MockHasMouseUpHandlers[ROWS][COLS];
  MockHasClickHandlers prevMonthHandler = new MockHasClickHandlers();
  MockHasClickHandlers nextMonthHandler = new MockHasClickHandlers();
  MockHasChangeHandlers monthSelectHandler = new MockHasChangeHandlers();
  MockHasChangeHandlers yearSelectHandler = new MockHasChangeHandlers();
  MockHasMouseWheelHandlers wheelHandler = new MockHasMouseWheelHandlers();
  MockHasAllMouseEvents gridWrapperHandlers = new MockHasAllMouseEvents();
  ValueChangeHandler<DateRange> changeHandler;
  MockTimer dragToNextMonthTimer = new MockTimer();
  Integer todayRow;
  Integer todayCol;
  Integer hoverRow;
  Integer hoverCol;
  Integer currentMonth;
  Integer currentYear;
  int firstDayOfWeek;

  {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        dayClickHandlers[row][col] = new MockHasClickHandlers();
        dayOverHandlers[row][col] = new MockHasMouseOverHandlers();
        dayOutHandlers[row][col] = new MockHasMouseOutHandlers();
        dayDownHandlers[row][col] = new MockHasMouseDownHandlers();
        dayUpHandlers[row][col] = new MockHasMouseUpHandlers();
      }
    }
  }

  @Override
  public Widget asWidget() {
    return null;
  }

  @Override
  public void markEvent(int row, int col, String label) {
    if (events[row][col][0] == null) {
      events[row][col][0] = label;
    } else {
      events[row][col][1] = label;
    }
  }

  @Override
  public void markPublicHoliday(int row, int col, String label) {
    if (markedHolidays[row][col] != null) {
      label = markedHolidays[row][col] + "," + label;
    }
    markedHolidays[row][col] = label;
  }

  @Override
  public void select(int row, int col) {
    selectedDays[row][col] = true;
  }

  @Override
  public void markToday(int row, int col) {
    todayRow = row;
    todayCol = col;
  }

  @Override
  public void setDay(int row, int col, int day) {
    days[row][col] = day;
  }

  @Override
  public void setDayFromOtherMonth(int row, int col, int day) {
    days[row][col] = day;
  }

  @Override
  public HasClickHandlers getDayAsClickSource(int row, int col) {
    return dayClickHandlers[row][col];
  }

  @Override
  public void deselect(int row, int col) {
    selectedDays[row][col] = false;
  }

  @Override
  public void dehover(int row, int col) {
    hoverRow = null;
    hoverCol = null;
  }

  @Override
  public HasMouseOutHandlers getDayAsMouseOutSource(int row, int col) {
    return dayOutHandlers[row][col];
  }

  @Override
  public HasMouseOverHandlers getDayAsMouseOverSource(int row, int col) {
    return dayOverHandlers[row][col];
  }

  @Override
  public void hover(int row, int col) {
    hoverRow = row;
    hoverCol = col;
  }

  @Override
  public HasMouseDownHandlers getDayAsMouseDownSource(int row, int col) {
    return dayDownHandlers[row][col];
  }

  @Override
  public HasMouseUpHandlers getDayAsMouseUpSource(int row, int col) {
    return dayUpHandlers[row][col];
  }

  @Override
  public HasClickHandlers getNextMonthButton() {
    return nextMonthHandler;
  }

  @Override
  public HasClickHandlers getPrevMonthButton() {
    return prevMonthHandler;
  }

  @Override
  public int getCurrentMonth() {
    return currentMonth;
  }

  @Override
  public int getCurrentYear() {
    return currentYear;
  }

  @Override
  public HasChangeHandlers getMonthSelect() {
    return monthSelectHandler;
  }

  @Override
  public HasChangeHandlers getYearsSelect() {
    return yearSelectHandler;
  }

  @Override
  public void setCurrentMonth(int month) {
    currentMonth = month;
  }

  @Override
  public void setCurrentYear(int year) {
    currentYear = year;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<DateRange> handler) {
    this.changeHandler = handler;
    ValueChangeEvent.getType();
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void fireEvent(GwtEvent<?> event) {
    changeHandler.onValueChange((ValueChangeEvent<DateRange>) event);
  }

  @Override
  public HasMouseWheelHandlers getMouseWheelSource() {
    return wheelHandler;
  }

  @Override
  public int getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  @Override
  public void unmarkEvent(int row, int col, String label) {
    for (int i = 0; i < events[row][col].length; i++) {
      if (label.equals(events[row][col][i])) {
        events[row][col][i] = null;
      }
    }
  }

  @Override
  public Set<PublicHoliday> getPublicHolidays() {
    return holidays;
  }

  @Override
  public Timer getDragToNextMonthTimer() {
    return dragToNextMonthTimer;
  }

  @Override
  public HasAllMouseHandlers getGridWrapper() {
    return gridWrapperHandlers;
  }
}