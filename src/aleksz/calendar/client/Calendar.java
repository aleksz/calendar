package aleksz.calendar.client;

import aleksz.utils.client.Date;
import aleksz.utils.client.DateRange;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author aleksz
 *
 */
public interface Calendar extends HasValue<DateRange> {

  void prevMonth();

  void nextMonth();

  void prevYear();

  void nextYear();

  void unmarkEvent(DateRange range, String label);

  void markEvent(DateRange range, String label);

  void markEvent(Date date, String label);

  Widget asWidget();
}
