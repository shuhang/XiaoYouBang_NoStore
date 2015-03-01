package self.chooseimage;import java.io.File;import java.io.FilenameFilter;import java.util.ArrayList;import java.util.HashSet;import java.util.List;import android.annotation.SuppressLint;import android.app.Activity;import android.app.ProgressDialog;import android.content.ContentResolver;import android.content.Intent;import android.database.Cursor;import android.net.Uri;import android.os.Bundle;import android.os.Environment;import android.os.Handler;import android.provider.MediaStore;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.Button;import android.widget.ListView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.tool.MyApplication;@SuppressLint("HandlerLeak")public class FileListActivity extends Activity{	private ListView mListDir;		private ProgressDialog mProgressDialog;		public static List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();	public static int selected = -1;		private Button buttonBack;		int totalCount = 0;	/**	 * 临时的辅助类，用于防止同一个文件夹的多次扫描	 */	private HashSet<String> mDirPaths = new HashSet<String>();		private Handler mHandler = new Handler()	{		public void handleMessage(android.os.Message msg)		{			mProgressDialog.dismiss();			// 为View绑定数据			mListDir.setAdapter(new CommonAdapter<ImageFloder>( FileListActivity.this, mImageFloders, R.layout.list_dir_item)					{						public void convert(ViewHolder helper, ImageFloder item)						{							helper.setText(R.id.id_dir_item_name, item.getName());							helper.setImageByUrl(R.id.id_dir_item_image,item.getFirstImagePath());							helper.setText(R.id.id_dir_item_count, item.getCount() + "张");						}					});		}	};		protected void onCreate(Bundle savedInstanceState)	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );		setContentView( R.layout.list_dir );				mImageFloders.clear();				mListDir = (ListView) findViewById(R.id.id_list_dir);				mListDir.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick(AdapterView<?> parent, View view,int position, long id)				{					selected = position;					startActivityForResult( new Intent( FileListActivity.this, ChooseImageActivity.class ), 0 );				}			}		);				buttonBack = ( Button ) findViewById( R.id.list_dir_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					finish();				}			}		);				getImages();	}		private void getImages()	{		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))		{			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();			return;		}		// 显示进度条		mProgressDialog = ProgressDialog.show(this, null, "正在加载...");		new Thread		(			new Runnable()			{				public void run()				{					String firstImage = null;						Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;					ContentResolver mContentResolver = FileListActivity.this							.getContentResolver();						// 只查询jpeg和png的图片					Cursor mCursor = mContentResolver.query(mImageUri, null,							MediaStore.Images.Media.MIME_TYPE + "=? or "									+ MediaStore.Images.Media.MIME_TYPE + "=?",							new String[] { "image/jpeg", "image/png" },							MediaStore.Images.Media.DATE_MODIFIED);					while (mCursor.moveToNext())					{						// 获取图片的路径						String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));						// 拿到第一张图片的路径						if (firstImage == null)							firstImage = path;						// 获取该图片的父路径名						File parentFile = new File(path).getParentFile();						if (parentFile == null)							continue;						String dirPath = parentFile.getAbsolutePath();						ImageFloder imageFloder = null;						// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）						if (mDirPaths.contains(dirPath))						{							continue;						} 						else						{							mDirPaths.add(dirPath);							// 初始化imageFloder							imageFloder = new ImageFloder();							imageFloder.setDir(dirPath);							imageFloder.setFirstImagePath(path);						}							int picSize = parentFile.list(new FilenameFilter()						{							public boolean accept(File dir, String filename)							{								if (filename.endsWith(".jpg")										|| filename.endsWith(".PNG")										|| filename.endsWith(".JPG")										|| filename.endsWith(".JPEG")										|| filename.endsWith(".png")										|| filename.endsWith(".jpeg"))									return true;								return false;							}						}).length;						totalCount += picSize;							imageFloder.setCount(picSize);						mImageFloders.add(imageFloder);					}										List< Integer > indexs = new ArrayList< Integer >();					int count = mImageFloders.size();					for( int i = 0; i < count; i ++ )					{						if( mImageFloders.get( i ).getDir().endsWith( "Camera" ) )						{							indexs.add( i );						}					}					count = indexs.size();					for( int i = 0; i < count; i ++ )					{						ImageFloder folder = mImageFloders.get( indexs.get( i ) );						mImageFloders.remove( i );						mImageFloders.add( 0, folder );					}										mCursor.close();					// 扫描完成，辅助的HashSet也就可以释放内存了					mDirPaths = null;					// 通知Handler扫描图片完成					mHandler.sendEmptyMessage(0x110);				}			}		).start();	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );				if( resultCode == 1 )		{			setResult( 2 );			finish();		}	}}