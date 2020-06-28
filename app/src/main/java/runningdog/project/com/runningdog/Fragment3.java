package runningdog.project.com.runningdog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import runningdog.project.com.runningdog.db.DBHelper;


public class Fragment3 extends Fragment {

    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    TextView mTvSendData;

    TextView minTextView;
    TextView secTextView;
    TextView milliTextView;


    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnConnect;
    Button mBtnSendData;

    FloatingActionButton mBtnStart;
    FloatingActionButton mBtnReset;
    FloatingActionButton mBtnPause;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    int stepCount = 0;
    Boolean running = false;
    private Thread timeThread = null;
    private Boolean isRunning = false;

    int mSec;
    int sec ;
    int min ;
    int i = 0;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;


    private AppCompatActivity activity;

    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //스마트폰 - 아두이노간의 데이터 전송을 위한 상수

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_fragment3, container, false);
        return rootView;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DBHelper dbHelper = new DBHelper(getActivity());
/*
        mTvBluetoothStatus = (TextView) getView().findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = (TextView) getView().findViewById(R.id.tvReceiveData);
        mTvSendData = (EditText) getView().findViewById(R.id.tvSendData);
        mBtnBluetoothOn = (Button) getView().findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = (Button) getView().findViewById(R.id.btnBluetoothOff);
*/
        mTvReceiveData = (TextView) getView().findViewById(R.id.tvReceiveData);
        mBtnConnect = (Button) getView().findViewById(R.id.btnConnect);

      //  mBtnSendData = (Button) getView().findViewById(R.id.btnSendData);
        mBtnStart = (FloatingActionButton) getView().findViewById(R.id.start_btn);
        mBtnReset = (FloatingActionButton) getView().findViewById(R.id.reset_btn);
        mBtnPause = (FloatingActionButton) getView().findViewById(R.id.reset_pause);

        minTextView = (TextView) getView().findViewById(R.id.minTextView);
        secTextView = (TextView) getView().findViewById(R.id.secTextView);
        milliTextView = (TextView) getView().findViewById(R.id.milliTextView);

        /*장치가 블루투스 기능을 지원하는지 알아오는 메소드*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* 버튼들이 클릭되었을 때 발생하는 이벤트를 구현한 리스너 */


        //시작
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                mBtnPause.setVisibility(View.VISIBLE);
                mBtnStart.setVisibility(View.INVISIBLE);
                timeThread = new Thread(new timeThread());

                isRunning = true;

                timeThread.start();
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                if (isRunning) {
                    mBtnPause.setImageResource(R.drawable.ic_pause_black_24dp);
                } else {
                    mBtnPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            }
        });


        //중지
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                if(timeThread != null){
                    isRunning = false;
                    mBtnStart.setVisibility(View.VISIBLE);
                    mBtnPause.setVisibility(View.INVISIBLE);
                    minTextView.setText("0");
                    secTextView.setText("0");
                    milliTextView.setText("0");
                    mTvReceiveData.setText("0");

                    long now = System.currentTimeMillis();
                    Date date = new Date(now);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                    String etDate = dateFormat.format(date);
                    String etTime = timeFormat.format(date);

                    Toast.makeText(activity, etDate + "\n" + etTime, Toast.LENGTH_LONG).show();
                    dbHelper.insert(etDate, etTime, stepCount);

                    timeThread.interrupt();
                    mSec = 0;
                    sec = 0;
                    min = 0;
                    i = 0;
                    stepCount = 0;

                }




            }
        });


        /*
        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });
        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });
        */

        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        /*
        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = Integer.parseInt(mTvSendData.getText().toString());
                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                    mTvSendData.setText("");
                }
            }
        });
    */
        /*블루투스 핸들러로 블루투스 연결 뒤 수신된 데이터를 읽어와
        ReceiveData 텍스트뷰에 표시 */
        mBluetoothHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                   // String readM = readMessage.substring(0,0).trim();
                    //Toast.makeText(getActivity(),readM,Toast.LENGTH_LONG).show();

                    if(isRunning){
                        //Toast.makeText(getActivity(),readM,Toast.LENGTH_LONG).show();
                        stepCount += 1;
                        mTvReceiveData.setText(Integer.toString(stepCount));
                        /*
                        if(readM.equals("1")){
                            stepCount += 1;
                            mTvReceiveData.setText(Integer.toString(stepCount));
                    }*/

                    }
                    else{
                        Toast.makeText(getActivity(),"정지 상태입니다",Toast.LENGTH_SHORT).show();
                    }
                    //stepCount ++;
                    //mTvReceiveData.setText(stepCount);

                }
            }
        };

/*
         timeHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int mSec = msg.arg1 % 100; //millisec
                int sec = (msg.arg1 / 100) % 60; //sec
                int min = (msg.arg1 / 100) / 60; //minute
                //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간
                if(sec/60 >=1 ){sec %=60;}

                minTextView.setText(String.valueOf(min));
                secTextView.setText(String.valueOf(sec));
                milliTextView.setText(String.valueOf(mSec));
            }
        };
*/


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity)
            activity = (AppCompatActivity) context;
    }

/*블루투스 ON 버튼을 누르면 동작하는 메소드로 최초에 "mBluetoothAdapter == null" 통하여 블루투스를 지원하는 기기인지 아닌지 판별할 수 있습니다.
지원하지 않는다면 finish() 같은 메소드로 애플리케이션을 종료하는 등의 작업을 수행할 수 있습니다.
지원하는 기기라면 블루투스가 활성화 되어 있는지 아닌지 또한 isEnabled() 메소드로 확인하여 각각에 따른 기능을 넣어주었습니다.
비활성화 되어 있다면 Intent 를 이용하여 활성화 창을 띄워 onActivityResult 에서 결과를 처리하게끔 하였습니다.*/

    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("활성화");
            } else {
                Toast.makeText(getActivity(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }




/*블루투스를 비활성화하는 메소드입니다.
 disable(); 메소드를 통하여 블루투스를 비활성화 할수 있으며
  그 외 코드는 블루투스 활성화 메소드랑 거의 비슷.*/


    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getActivity(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("비활성화");
        } else {
            Toast.makeText(getActivity(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }



    /*블루투스 ON 메소드에서 Intent 로 받은 결과를 처리하는 메소드(?)입니다.
    블루투스 활성화 창에서 확인을 누르면 mTvBluetoothStatus 텍스트뷰에
    “활성화”를 취소를누르면 “비활성화”를 표시하게끔 처리하였습니다.*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == getActivity().RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getActivity(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("활성화");
                } else if (resultCode == getActivity().RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getActivity(), "취소", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("비활성화");
                }
                break;
            //RESULT_OK is constant of Activity class. In Activity class you can access directly but in other classes you need to write class name (Activity) also.
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*우선 블루투스가 활성화 상태인지 확인하고*/
    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            //페어링된 장치가 존재시 새로운 알람창 객체를 생성하여 "장치선택"타이틀과 각 페어링된 장치명을 추가.
            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                //페어링된 장치 수를 얻어와서 각 장치를 누르면 장치 명을 매개변수로 사용하여 connectSelectedDevice 메소드로 전달해주는 클릭 이벤트를 추가해주었습니다.

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                //위에서 리스트로 추가된 알람창을 실제로 띄어줍니다.
            } else {
                Toast.makeText(getActivity(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /*실제로 블루투스 장치와 연결하는 부분입니다.
    우리가 listPairedDevices 메소드를 통하여 전달받은 매개변수 값은 장치 이름입니다.
     우리가 연결에 필요한 값은 장치의 주소로 for 문으로 페어링된 모든 장치를 검색을 하면서
      매개 변수 값과 비교하여 같다면 그 장치의 주소 값을 얻어옵니다.*/

    void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getActivity(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //수신받은 데이터는 언제들어올 지 모르니 항상 확인
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
        //처리된 데이터가 존재하면 데이터를 읽어오는 작업
            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        //데이터 전송을 위한 메소드
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getActivity(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getActivity(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

    }

    public class timeThread implements Runnable {
        @Override
        public void run() {
            i = 0;
            while (true) {
                while (isRunning) { //일시정지를 누르면 멈춤
                    //Message msg = new Message();
                    //msg.arg1 = i++;
                    //timeHandler.sendMessage(msg);
                    i++;
                    mSec = i % 100; //millisec
                    sec = (i / 100) % 60; //sec
                    min = (i / 100) / 60; //minute
                    //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간
                    if(sec/60 >=1 ){sec %=60;}

                    if(activity == null){ return;}
                    activity.runOnUiThread(new Runnable() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void run() {
                            minTextView.setText(String.valueOf(min));
                            secTextView.setText(String.valueOf(sec));
                            milliTextView.setText(String.valueOf(mSec));
                            mBtnPause.setVisibility(View.VISIBLE);
                            mBtnStart.setVisibility(View.INVISIBLE);
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        activity.runOnUiThread(new Runnable() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void run() {
                                mBtnStart.setVisibility(View.VISIBLE);
                                mBtnPause.setVisibility(View.INVISIBLE);
                                minTextView.setText("0");
                                secTextView.setText("0");
                                milliTextView.setText("0");
                                mTvReceiveData.setText("0");
                                stepCount = 0;
                                mSec = 0;
                                sec = 0;
                                min = 0;
                                i = 0;
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    }
                }
            }
        }

    }

}



//fragment에선 getActivity() https://jain5480.blog.me/221195553416

//https://kingpiggylab.tistory.com/127

//https://kingpiggylab.tistory.com/127

