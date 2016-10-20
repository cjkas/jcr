package cz.slaw.jcr;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.AudioPlayerHelper;
import cz.slaw.jcr.helpers.SettingsHelper;

public class AudioPlayerActivity extends Activity implements OnSeekBarChangeListener,OnCompletionListener {
	
	private static final Logger log = LoggerFactory.getLogger(AudioPlayerActivity.class);
	
	private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private SeekBar songProgressBar;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private final Handler mHandler = new Handler();
    private AudioPlayerHelper utils;
    private final int seekForwardTime = 5000; // 5000 milliseconds
    private final int seekBackwardTime = 5000; // 5000 milliseconds

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_player);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.sp_play_pause);
        btnForward = (ImageButton) findViewById(R.id.sp_forward);
        btnBackward = (ImageButton) findViewById(R.id.sp_rewind);
        songProgressBar = (SeekBar) findViewById(R.id.sp_seek_bar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.sp_current);
        songTotalDurationLabel = (TextView) findViewById(R.id.sp_total);
 
        // Mediaplayer
        mp = new MediaPlayer();
        utils = new AudioPlayerHelper();
 
        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this);
 
		Bundle b = getIntent().getExtras();

        DbRecord record = (DbRecord) b.get("record");
        // By default play first song
        playSong(record);
 
        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // check for already playing
            	if(mp!=null)
                if(mp.isPlaying()){
                    mp.pause();
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    // Changing button image to play button
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                }else{
                    // Resume song
                	 if(mp.getCurrentPosition()==mp.getDuration()){
                     	mp.seekTo(0);
                     }
                    mp.start();
                    updateProgressBar();
                    // Changing button image to pause button
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
 
            }
        });
 
        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mp.getDuration()){
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                }else{
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
                if(!mp.isPlaying()){
                updateProgressBar();
                mHandler.removeCallbacks(mUpdateTimeTask);
                }
            }
        });
 
        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }
                if(!mp.isPlaying()){
                updateProgressBar();
                mHandler.removeCallbacks(mUpdateTimeTask);
                }
 
            }
        });
        
    }
    
    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(DbRecord record){
        // Play song
        try {
        	File file = new File(SettingsHelper.getStorageDir(this), record.getPath());
            mp.reset();
            mp.setDataSource(file.getAbsolutePath());
            mp.prepare();
            mp.start();
 
            // Changing Button Image to pause image
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);
 
            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
 
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            log.error("err",e);
        } catch (IllegalStateException e) {
        	log.error("err",e);
        } catch (IOException e) {
        	log.error("err",e);
        }
    }
 
    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
    private final Runnable mUpdateTimeTask = new Runnable() {
           @Override
		public void run() {
               long totalDuration = mp.getDuration();
               long currentDuration = mp.getCurrentPosition();
 
               // Displaying Total Duration time
               songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
 
               // Updating progress bar
               int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);
 
               // Running this thread after 100 milliseconds
               mHandler.postDelayed(this, 100);
           }
        };
 
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    	if(mp!=null)
    		if(!mp.isPlaying()){
    			mp.start();
    			btnPlay.setImageResource(android.R.drawable.ic_media_pause);
    		}
    }
 
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
 
    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
 
        // forward or backward to certain seconds
        mp.seekTo(currentPosition);
 
        // update timer progress again
        updateProgressBar();
    }
 
    @Override
    public void onBackPressed() {
       finish();
    }
 
    @Override
	public void onCompletion(MediaPlayer mp) {
    	mHandler.removeCallbacks(mUpdateTimeTask);
    	// Changing Button Image to pause image
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mp!=null && mp.isPlaying()){
            mp.pause();
            mHandler.removeCallbacks(mUpdateTimeTask);
            // Changing button image to play button
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
    	}
    	
    }
    
    @Override
     public void onDestroy(){
     super.onDestroy();
     	mHandler.removeCallbacks(mUpdateTimeTask);
        mp.release();
     }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent act = new Intent(this, SettingsActivity.class);
			startActivity(act);
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


}
