package cmput301W12.android.project;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


public class AlarmController extends Activity implements FView<DbController>
{
	private static final String MEDIA_PLAYER = null;
	Toast mToast;
	MediaPlayer mp;
	Timestamp timestamp;
	EditText alarmtext;
	Button alarmtime;
	Button alarmdate;
	
	private int alarm_type ;
	private int theYear = -1;
	private int theMonth = -1;
	private int theDay = -1;
	private int theHour = -1;
	private int theMinute = -1;
	
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_controller);
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.alarm_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        mp = new MediaPlayer();
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE); 
        try {
			mp.setDataSource(this, alert);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.create_alarm_button);
        button.setOnClickListener(createalarmListener);
        
        alarmtime = (Button)findViewById(R.id.timePicker);
        alarmdate = (Button)findViewById(R.id.datepicker);
        alarmtext = (EditText)findViewById(R.id.alarm_text_editview);
        
        alarmtime.setOnClickListener(invokeTimePicker);
        alarmdate.setOnClickListener(invokeDatePicker);
        
        /*button = (Button)findViewById(R.id.start_repeating);
        button.setOnClickListener(mStartRepeatingListener);
        button = (Button)findViewById(R.id.stop_repeating);
        button.setOnClickListener(mStopRepeatingListener);*/
        
    }
    
    OnDateSetListener odsListener = new OnDateSetListener()
    {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			// TODO Auto-generated method stub
			
			theYear = year;
			theMonth = monthOfYear + 1;
			theDay = dayOfMonth;
			Calendar cal = Calendar.getInstance();
			
			if(theHour == -1) {
				cal.get(Calendar.HOUR);
			}
			
			if(theMinute == -1) {
				cal.get(Calendar.MINUTE);
			}
			
			timestamp = new Timestamp(theYear, theMonth, theDay, theHour, theMinute, 0, 0);
		}
    };
    
    private OnTimeSetListener otsListener = new OnTimeSetListener() 
    {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			
			theHour = hourOfDay;
			theMinute = minute;
			Calendar cal = Calendar.getInstance();
			
			if(theYear == -1) {
				cal.get(Calendar.YEAR);
			}
			
			if(theMonth == -1) {
				cal.get(Calendar.MONTH);
			}
			
			if(theDay == -1) {
				cal.get(Calendar.DAY_OF_MONTH);
			}
			
			timestamp = new Timestamp(theYear, theMonth, theDay, theHour, theMinute, 0, 0);
		}

		
    	
    };

    private OnClickListener createalarmListener = new OnClickListener() {
        public void onClick(View v) {
            
        	if(theYear == -1 || theMonth == -1 || theDay == -1 || theHour == -1 || theMinute == -1) {
        		mToast = Toast.makeText(AlarmController.this, "Stopping Repeating Shots",
                        Toast.LENGTH_LONG);
                mToast.show();
        	}
        	
        	else if(alarmtext.getText().toString() == "") {
        		mToast = Toast.makeText(AlarmController.this, "Stopping Repeating Shots",
                        Toast.LENGTH_LONG);
                mToast.show();
        	}
        	
        	else {
        		Alarm alarm = new Alarm(timestamp, alarmtext.getText().toString());
        		
        		FController controller =  SkinObserverApplication.getSkinObserverController(AlarmController.this);
        		controller.addAlarm(alarm);
        	}
        }
    };

    private OnClickListener invokeDatePicker = new OnClickListener() {
        public void onClick(View v) {
        
        	Calendar cal = Calendar.getInstance();
        	DatePickerDialog dpDialog = new DatePickerDialog(AlarmController.this,
        			odsListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 
        			cal.get(Calendar.DAY_OF_MONTH));
        			dpDialog.show();
        }
    };
    
    private OnClickListener invokeTimePicker = new OnClickListener() {
        public void onClick(View v) {
        
        	Calendar cal = Calendar.getInstance();
        	TimePickerDialog tpDialog = new TimePickerDialog(AlarmController.this,
        			otsListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
        			tpDialog.show();
        }
    };

    private OnClickListener mStopRepeatingListener = new OnClickListener() {
        public void onClick(View v) {
            // Create the same intent, and thus a matching IntentSender, for
            // the one that was scheduled.
            Intent intent = new Intent(AlarmController.this, AlarmReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);

            // And cancel the alarm.
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, "Stopping Repeating Shots",
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
          
          if (pos == 0) {
        	  alarm_type = 0;
          }
          
          else if (pos == 1) {
        	  alarm_type = 1;
          }
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }

	public void setonetimeAlarm() {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent(this, AlarmReceiver.class);
		Bundle bundle = new Bundle();
		//bundle.putParcelable(MEDIA_PLAYER, (Parcelable) mp);
		//intent.putExtras(bundle);
        PendingIntent sender = PendingIntent.getBroadcast(this,
                0, intent, 0);

        // We want the alarm to go off 30 seconds from now.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 30);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

        // Tell the user about what we did.
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(AlarmController.this, "Testing One Shot",
                Toast.LENGTH_LONG);
        mToast.show();
		
	}

	public void setrepeatingAlarm() {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this,
                0, intent, 0);

        // We want the alarm to go off 30 seconds from now.
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 15*1000;

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 15*1000, sender);

        // Tell the user about what we did.
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, "Testing Repeating Shots",
                Toast.LENGTH_LONG);
        mToast.show();
		
	}

	@Override
	public void update(DbController model) {
		// TODO Auto-generated method stub
		
	}
    
    

}
