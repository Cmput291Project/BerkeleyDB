package cmput301W12.android.project;

/**	
 * @author : Tanvir Sajed
 * @date : Mar 16, 2012
 * 
 *	ViewPhoto.class is the activity which is responsible for showing the photo
 * in a very big size for the viewer. The Activity will also show some other information
 * pertaining to the photo, including the photo's name, photo's group, photo's skin 
 * condition. This activity completes the user requirement of viewing a photo the user
 * has taken before. It basically creates Bitmap image from serialized photo object
 * saved in an intent sent from PhotoListActivity.class, the photo that the user has
 * selected before. That bitmap image is seen in ImageView in this activity.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewPhotoActivity extends Activity implements FView<DbController>
{

	private ImageView newimage;
	private TextView timestamp;
	private TextView skincondition;
	private TextView group;
	private Photo photo = null;
	
	public static final int ID_CREATE_ANNOTATION = Menu.FIRST;

	private static void saveBMP( File intentPicture, Bitmap ourBMP) throws IOException, FileNotFoundException {
		OutputStream out = new FileOutputStream(intentPicture);
		ourBMP.compress(Bitmap.CompressFormat.JPEG, 75, out);
		out.close();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photo);
		
		photo = (Photo) getIntent().getSerializableExtra("PHOTO");
		newimage = (ImageView) findViewById(R.id.imageView1);
		timestamp = (TextView) findViewById(R.id.viewphototext1);
		skincondition = (TextView) findViewById(R.id.viewphototext2);
		group = (TextView) findViewById(R.id.viewphototext3);

		this.getPhotos();
		this.getTimeStamp();


	}

	public void getPhotos(){
		try{
			//photo = (Photo)getIntent().getSerializableExtra(PhotoListActivity.PHOTO);
			String location = photo.getLocation();
			Uri uri = Uri.parse(location);
			Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//			File newFile = new File(uri.getPath());
//			Bitmap bitmap = BogoPicGen.generateBitmap(400, 400);
//			ViewPhoto.saveBMP(newFile, bitmap);
			newimage.setImageBitmap(bitmap);
		}catch(Exception ex){
			System.out.println("Can't show the photo!");
		}

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ID_CREATE_ANNOTATION, 0, "View Photo Annotation");
        return true;
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case ID_CREATE_ANNOTATION:
			showAnnotation(photo, this);
			//return true;
		}

            return super.onMenuItemSelected(featureId, item);
	}
	
	
	public void showAnnotation(final Photo photo, Context context) {
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);
		popupBuilder.setTitle("Annotation");
		final EditText annotation = new EditText(context);
		//final Photo photo = (Photo)getIntent().getSerializableExtra(PhotoListActivity.PHOTO);
		if (photo.getAnnotation().equals(""))
		{
			annotation.setSingleLine();
			annotation.setText("");
		}
		else
		{
			annotation.setSingleLine();
			annotation.setText(photo.getAnnotation());
		}
		popupBuilder.setView(annotation);
		popupBuilder.setNeutralButton("Confirm", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//When click confirm save the annotation into the photo?
				photo.setAnnotation(annotation.getText().toString());
				
			}
		});
		popupBuilder.create();
		popupBuilder.show();
	}
	
	public void getTimeStamp() {
		//Photo photo = (Photo) getIntent().getSerializableExtra(PhotoListActivity.PHOTO);
		Timestamp ts = photo.getTimeStamp();
		String time = ts.toString();
		timestamp.setText(time);
	}

	@Override
	public void update(DbController model)
	{
			this.getPhotos();

	}


}
