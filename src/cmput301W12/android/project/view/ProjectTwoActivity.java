package cmput301W12.android.project.view;

//import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import cmput301W12.android.project.R;

    /**
     * The ProjectTwoActivity.java file displays a menu that allows the user
     * to either take a picture, or to view their photo library.
     * 
     * @author Stephen Romansky
     *
     */


public class ProjectTwoActivity extends Activity
{

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button takeAPhoto = (Button) this.findViewById(R.id.takeAPhoto);
        
        takeAPhoto.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                /* Add code here to start up camera activity */
                //Intent getAPhoto = new Intent(this, )
                //Make a camera controller
                //TakeAPhotoActivity myCamera = new TakeAPhotoActivity();
                //myCamera.getPhoto();
                
                
                // TODO Auto-generated method stub
                
            }
        });
    }

}
