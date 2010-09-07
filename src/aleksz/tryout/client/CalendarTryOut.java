package aleksz.tryout.client;

import aleksz.calendar.client.CalendarImpl;
import aleksz.utils.client.Date;
import aleksz.utils.client.DateRange;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class CalendarTryOut implements EntryPoint {

  public void onModuleLoad() {
    CalendarImpl c = new CalendarImpl();
    c.markEvent(new Date(2009, 9, 15), "My event");
    c.markEvent(new Date(2009, 9, 15), "Another event");
    c.markEvent(new DateRange(new Date(2009, 9, 14), new Date(2009, 9, 17)), "range event");
    c.addValueChangeHandler(new ValueChangeHandler<DateRange>() {

      @Override
      public void onValueChange(ValueChangeEvent<DateRange> event) {
        Window.alert(String.valueOf(event.getValue()));
      }

    });

    RootPanel.get().add(c.asWidget());
  }
}
