package com.wowwee.miposaursample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wowwee.bluetoothrobotcontrollib.RobotCommand;
//import com.wowwee.bluetoothrobotcontrollib.MipCommandValues;
import com.wowwee.bluetoothrobotcontrollib.miposaur.sdk.MiposaurCommandValues;
//import com.wowwee.bluetoothrobotcontrollib.MipRobotSound;
import com.wowwee.bluetoothrobotcontrollib.miposaur.sdk.MiposaurRobotSound;
//import com.wowwee.bluetoothrobotcontrollib.sdk.MipRobotFinder;
import com.wowwee.bluetoothrobotcontrollib.miposaur.sdk.MiposaurRobotFinder;
//import com.wowwee.bluetoothrobotcontrollib.sdk.MipRobot;
import com.wowwee.bluetoothrobotcontrollib.miposaur.sdk.MiposaurRobot;
import com.wowwee.bluetoothrobotcontrollib.miposaur.sdk.MiposaurRobot.MiposaurRobotInterface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

public class MainMenuActivity extends FragmentActivity implements MiposaurRobotInterface{

	private BluetoothAdapter mBluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_main_menu);
		
		final BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		// Set BluetoothAdapter to MipRobotFinder
		MiposaurRobotFinder.getInstance().setBluetoothAdapter(mBluetoothAdapter);
		
		// Set Context to MipRobotFinder
		MiposaurRobotFinder finder = MiposaurRobotFinder.getInstance();
		finder.setApplicationContext(getApplicationContext());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.registerReceiver(mMipFinderBroadcastReceiver, MiposaurRobotFinder.getMiposaurRobotFinderIntentFilter());
		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
           if (!mBluetoothAdapter.isEnabled()) {
               TextView noBtText = (TextView)this.findViewById(R.id.no_bt_text);
               noBtText.setVisibility(View.VISIBLE);
           }
		}
		
		// Search for mip
		MiposaurRobotFinder.getInstance().clearFoundMiposaurList();
		scanLeDevice(false);
//		updateMipList();
		scanLeDevice(true);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.unregisterReceiver(mMipFinderBroadcastReceiver);
		for(MiposaurRobot miposaur : MiposaurRobotFinder.getInstance().getMiposaursConnected()) {
			miposaur.readMiposaurHardwareVersion();
			miposaur.disconnect();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		System.exit(0);
	}
	
	public void buttonOnclickHandler(View view){
		
		int buttonID = view.getId();
		if ( buttonID == R.id.playsound)
		{
			List<MiposaurRobot> miposaurs = MiposaurRobotFinder.getInstance().getMiposaursConnected();
			for (MiposaurRobot miposaur : miposaurs) {
				miposaur.miposaurPlaySound(MiposaurRobotSound.create(MiposaurCommandValues.kMiposaurSoundFile_DINO_APPMODE_1));
			}
		}
		else if ( buttonID == R.id.changechest)
		{
			List<MiposaurRobot> miposaurs = MiposaurRobotFinder.getInstance().getMiposaursConnected();
			int colorIndex = getResources().getColor(R.color.white_color);
			for (MiposaurRobot miposaur : miposaurs) {
				// Pass RGB color to define what color you want the Chest RGB to turn
				miposaur.setMiposaurChestRGBLedWithColor((byte)Color.red(colorIndex), (byte)Color.green(colorIndex), (byte)Color.blue(colorIndex), (byte) 1);
			}
		}else if ( buttonID ==  R.id.sit)
		{
			List<MiposaurRobot> miposaurs = MiposaurRobotFinder.getInstance().getMiposaursConnected();
			for (MiposaurRobot miposaur : miposaurs) {
				miposaur.miposaurFalloverWithStyle(MiposaurCommandValues.kMiposaurPositionValue.kMiposaurPositionOnFontCantGetup.value);
			}
		}else if ( buttonID ==  R.id.stand)
		{
			List<MiposaurRobot> miposaurs = MiposaurRobotFinder.getInstance().getMiposaursConnected();
			for (MiposaurRobot miposaur : miposaurs) {
				miposaur.miposaurFalloverWithStyle(MiposaurCommandValues.kMiposaurPositionValue.kMiposaurPositionUpright.value);
			}
		}else if ( buttonID ==  R.id.fallover)
		{
			List<MiposaurRobot> miposaurs = MiposaurRobotFinder.getInstance().getMiposaursConnected();
			for (MiposaurRobot miposaur : miposaurs) {
				miposaur.miposaurFalloverWithStyle(MiposaurCommandValues.kMiposaurPositionValue.kMiposaurPositionOnFront.value);
			}
		}else if ( buttonID ==  R.id.drive)
		{
			DriveViewFragment fragment = new DriveViewFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.view_id_drive_layout, fragment);
			transaction.attach(fragment);
			transaction.commit();
		}
		
	}

	private void scanLeDevice(final boolean enable) {
        if (enable) {
            MiposaurRobotFinder.getInstance().scanForMiposaurs();
        } else {
            MiposaurRobotFinder.getInstance().stopScanForMiposaurs();
        }
    }
	
	public void updateMipList()
	{
		//connect to first found mip
		List<MiposaurRobot> miposaurFoundList = MiposaurRobotFinder.getInstance().getMiposaursFoundList();
		for(MiposaurRobot mipRobot : miposaurFoundList) {
			connectToMiposaur(mipRobot);
			break;
		}
	}

	private void connectToMiposaur( final MiposaurRobot miposaurRobot) {

		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				miposaurRobot.setCallbackInterface(MainMenuActivity.this);
				miposaurRobot.connect(MainMenuActivity.this.getApplicationContext());
				TextView connectionView = (TextView)MainMenuActivity.this.findViewById(R.id.connect_text);
				connectionView.setText("Connecting: "+miposaurRobot.getName());
			}
		});
		
	}


	private final BroadcastReceiver mMipFinderBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MiposaurRobotFinder.MiposaurRobotFinder_MipFound.equals(action)) {
            	// Connect to miposaur
            	final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						 List<MiposaurRobot> miposaurFoundList = MiposaurRobotFinder.getInstance().getMiposaursFoundList();
						 if (miposaurFoundList != null && miposaurFoundList.size() > 0){
							 MiposaurRobot selectedMiposaurRobot = miposaurFoundList.get(0);
							  if (selectedMiposaurRobot != null){
								  connectToMiposaur(selectedMiposaurRobot);
							  }
						 }
					}
				}, 3000);
				 
            }
        }
	};

	@Override
	public void miposaurDeviceReady(MiposaurRobot sender) {
		// TODO Auto-generated method stub
		final MiposaurRobot robot = sender;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView connectionView = (TextView)MainMenuActivity.this.findViewById(R.id.connect_text);
				connectionView.setText("Connected: "+robot.getName());
			}
		});
	}

	@Override
	public void miposaurDeviceDisconnected(MiposaurRobot sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveBatteryLevelReading(MiposaurRobot miposaur, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceivePosition(MiposaurRobot miposaur, byte position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveToyActivationStatus(MiposaurRobot miposaur, boolean activated,
			boolean uploadedActivation, boolean hackerUartUsed, boolean uploadedHackerUartUsed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveHardwareVersion(int miposaurHardwareVersion, int miposaurVoiceFirmwareVersion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveSoftwareVersion(Date miposaurFirmwareVersionDate,
			int miposaurFirmwareVersionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveVolumeLevel(int miposaurVolume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveIRCommand(ArrayList<Byte> irDataArray, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotDidReceiveWeightReading(byte value, boolean leaningForward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void miposaurRobotIsCurrentlyInBootloader(MiposaurRobot miposaur, boolean isBootloader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean miposaurRobotBluetoothDidProcessedReceiveRobotCommand(MiposaurRobot miposaur,
			RobotCommand command) {
		// TODO Auto-generated method stub
		return false;
	}

}
