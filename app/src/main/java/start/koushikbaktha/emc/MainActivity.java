package start.koushikbaktha.emc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.PersistentCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DtcNumberCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListView.OnItemClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView lblBluetoothUpdate;
    private TextView currentRPM;
    boolean btDeviceFound = false;
    private Button btn_scan;
    private Button callRA;
    private Button nearAuto;
    private ListView listView;
    ArrayAdapter<String> listAdapter;
    private static final int REQUEST_PAIRED_DEVICE = 3;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> devicesArray;
    IntentFilter filter;
    BroadcastReceiver receiver;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final int SUCCESS_CONNECT = 0;
    public static final int MESSAGE_READ = 1;


    Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    // Write the method after the connection is successful
                    Log.e("My code is working", "inside switch case");
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getApplicationContext(), "Successfully connected", Toast.LENGTH_SHORT);
                    String s = "Successfully connected";
                    connectedThread.write(s.getBytes());
                    Log.e("My code is working", s);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
                    break;
                case 4:
                    Log.e("In case 4", "Need to connect with a OBD device");
                    Toast.makeText(getApplicationContext(), "Unable to create connection. Please try connecting with a OBD device",Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Log.e("In message Handler", "inside case 5");
                    callRA.setVisibility(View.INVISIBLE);
                    nearAuto.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    Log.e("In message handler","Inside case 6");
                    nearAuto.setVisibility(View.INVISIBLE);
                    callRA.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickFabEmail(view);
                Snackbar.make(view, "Email to: deepakraja90@gmail.com for any query!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    public void onClickFabEmail(View v){
        Snackbar.make(v, "Email to: deepakraja90@gmail.com for any query!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean basedonNavigationItemSelected() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e("My code is working", "In newly created function for layout");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {

            Log.e("Super is not working", "code is wrong");
            setContentView(R.layout.activity_main);
            basedonNavigationItemSelected();

            // Handle the Alert action*/
        } else if (id == R.id.nav_alerts) {

            setContentView(R.layout.alerts_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Log.e("My code is working", "In Alerts activity");
            lblBluetoothUpdate = (TextView) findViewById(R.id.lblBluetoothUpdate);
            btn_scan = (Button) findViewById(R.id.btn_scan);
            callRA = (Button)findViewById(R.id.callRA);

            listView = (ListView) findViewById(R.id.listView);
            nearAuto = (Button) findViewById(R.id.nearAuto);
            //currentRPM = (TextView) findViewById(R.id.currentRPM);
            basedonNavigationItemSelected();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean enableBT(View view) {
        Log.e("My code is working", "bluetooth button is working");

        if (mBluetoothAdapter == null) {
            Log.e("My code is working", "Device doesnot support bluetooth");
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                turnOnBT();
                return false;
            } else {
                lblBluetoothUpdate.setText("Bluetooth has been enabled, Please pair your vehicle bluetooth");
                btn_scan.setVisibility(View.VISIBLE);
                //getBTDevice(view);
            }
        }
        return true;
    }


    private void turnOnBT() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println(resultCode);

        if (resultCode == 0) {
            //When user clicks 'No' option in Bluetooth permission
            System.out.println("one");
            lblBluetoothUpdate.setText("Please Enable your bluetooth to receive updates from your vehicle");
            btn_scan.setVisibility(View.INVISIBLE);

        } else {

            System.out.println("two");
            lblBluetoothUpdate.setText("Bluetooth has been enabled, Please pair your vehicle bluetooth");
            btn_scan.setVisibility(View.VISIBLE);
        }

    }

    public void callRoadsideAssistance(View v){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:3098260905"));
        try{
            startActivity(callIntent);
        }catch (SecurityException e){
            Log.e("Exception occurred","problem");
        }
    }

    public void sampleButtonCounter(View v){
        for(int i=0;i<=10000;i++){
            currentRPM.setText("RPM is"+i);
        }
    }

    public void getBTDevice(View v) {
        Log.e("My code is working", "else condition is working");
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);

        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(this);

        devices = new ArrayList<BluetoothDevice>();
        pairedDevices = new ArrayList<String>();


        getPairedDevicesHistory();
        startDiscovery();
        receiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();


                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.e("getBTDevice", "Inside if of ACTION_FOUND");
                    btDeviceFound = true;
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    devices.add(device);
                    String s = "";
                    for (int a = 0; a < pairedDevices.size(); a++) {
                        if (device.getName().equals(pairedDevices.get(a))) {
                            //Appending New and paired devices

                            s = "(Paired)";

                            break;
                        }
                    }

                    listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());



                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {


                    Log.e("getBTDevice", "Inside else of ACTION_DISCOVERY_STARTED");


                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.e("getBTDevice", "Inside else of ACTION_DISCOVERY_FINISHED");
                    if (btDeviceFound == false){
                        Toast.makeText(getApplicationContext(), "There are no devices to show",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Scan complete",Toast.LENGTH_SHORT).show();
                    }

                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                    if (mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_OFF) {
                        turnOnBT();
                    }
                }
            }

        };
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }
    public void openMaps(View v){
        Intent mapsIntent = new Intent(v.getContext(),MapsActivity.class);
        startActivityForResult(mapsIntent,0);
    }
   private void startDiscovery() {

       mBluetoothAdapter.cancelDiscovery();
       mBluetoothAdapter.startDiscovery();
   }

    public void getPairedDevicesHistory() {
        pairedDevices.clear();
        Log.e("getPairedDevicesHistory", "inside paired devices history");
        devicesArray = mBluetoothAdapter.getBondedDevices();
        if (devicesArray.size() > 0) {
            Log.e("getPairedDevicesHistory", "inside if");
            for (BluetoothDevice device : devicesArray) {
                Log.e("getPairedDevicesHistory", "inside if1");
                pairedDevices.add(device.getName());
                Log.e("getPairedDevicesHistory", "inside if2");
            }


        } else {
            Log.e("getPairedDevicesHistory", "inside else");
            //Toast.makeText(getApplicationContext(), "There are no paired devices",Toast.LENGTH_SHORT).show();
        }
        Log.e("getPairedDevicesHistory", "Outside if");
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Log.e("getPairedDevicesHistory", "inside paired devices history");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (listAdapter.getItem(arg2).contains("Paired")) {
            //New code
            BluetoothDevice selectedDevice = devices.get(arg2);
            ConnectThread connect = new ConnectThread(selectedDevice);
            try {
                connect.start();

            }catch (Exception e)
            {
                Log.e("On item click", "Not able to connect");

            }

        } else {
            Toast.makeText(getApplicationContext(), "Please pair your device!!", Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectThread extends Thread {

        private BluetoothSocket mmSocket;
        private ConnectedThread ct;
//        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
           /* BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                Log.e("my code is working", "inside try");
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e("my code is working", "error is not resolved");
            }
            mmSocket = tmp;
*/
            //mmSocket = null;

            try {
                Log.e("","atleast going in try");
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                Log.e("", "Error creating socket");
            }

           /* try {
                Log.e("", "Connected");
                mmSocket.connect();

               Log.e("", "2nd Connected");
            } catch (IOException e) {
                Log.e("", e.getMessage());
                try {
                    Log.e("", "trying fallback...");

                    mmSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    mmSocket.connect();
                    Log.e("", "Connected");
                } catch (Exception e2) {
                    Log.e("", "Couldn't establish Bluetooth connection!");
                }
            }*/
        }


        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.e("Before connection", "Inside Try");
                mmSocket.connect();

            } catch (IOException connectException) {
                Log.e("Try connect", "Exception when trying ot connect");
                //Toast.makeText(getApplicationContext(),"Exception when trying to connect",Toast.LENGTH_SHORT);
                // Unable to connect; close the socket and get out
                try {
                    Log.e("Inside connected", "Exception when trying to close socket");
                    mmSocket.close();
                    mHandler.obtainMessage(4).sendToTarget();

                } catch (IOException closeException) {
                }
                return;
            }
            Log.e("After establish connect", "Connection Successful");
            ct = new ConnectedThread(mmSocket);
            // Do work to manage the connection (in a separate thread)
            // mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }


        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        // private final InputStream mmInStream;
        // private final OutputStream mmOutStream;

        protected ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            Log.e("Inside Connected", "Socket connection transfered to next thread");

            try {
                //ResetTroubleCodesCommand clear = new ResetTroubleCodesCommand();
                //clear.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                //String result = clear.getFormattedResult();
                String result;
                //Log.d("TESTRESET", "Trying reset result: " + result);
                new ObdResetCommand().run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                Log.e("OBD Reset cmd", "atleast resets");
                new EchoOffCommand().run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                Log.e("Echo off cmd", "atleast echos");
                new LineFeedOffCommand().run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                Log.e("Line feed off", "atleast feeds line");
                new TimeoutCommand(125).run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                Log.e("Timeout", "atleast timesout");
                new SelectProtocolCommand(ObdProtocols.AUTO).run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                Log.e("select protocol", "atleast selects");

                //Code for engine RPM, Speed, AirBag Deployment
                try {
                    RPMCommand engineRPM = new RPMCommand();
                    MassAirFlowCommand massAirFlow = new MassAirFlowCommand();
                    SpeedCommand engineSpeed = new SpeedCommand();
                    TroubleCodesCommand dtcCodesCommand = new TroubleCodesCommand();
                    DtcNumberCommand dashBoard = new DtcNumberCommand();
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.e("DTC","Inside Connected tread while");
                        engineRPM.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        String eRPM = engineRPM.getFormattedResult();
                        if (eRPM != null || eRPM != "")
                            Log.e("RPM",eRPM);

                        engineSpeed.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        String eSpeed = engineSpeed.getFormattedResult();
                        if (eSpeed != null || eSpeed != "")
                            Log.e("Engine Speed:",eSpeed);

                            //try {
                                dtcCodesCommand.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                                String dtcCodes = dtcCodesCommand.getFormattedResult();


                                if (dtcCodes != null || dtcCodes != "")
                                Log.e("DTC Codes:",dtcCodes);
                               // } catch (Exception e) {
                        else
                               Log.e("DTC Codes:", "No codes to show");
                               // e.printStackTrace();
                                //}

                        dashBoard.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        String dash = dashBoard.getFormattedResult();
                        Log.e("Dash board:", dash);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                //Code for engine RPM, Speed, AirBag Deployment
/*
                //Code for engine RPM command
                try{
                    RPMCommand engineRPMObdCommand = new RPMCommand();
                    int iRPM = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        engineRPMObdCommand.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        String eRPM = engineRPMObdCommand.getFormattedResult();

                        eRPM = eRPM.substring(0, eRPM.length() - 3);
                        Log.e("RPM in 1stwhile",eRPM);
                        iRPM = Integer.parseInt(eRPM);
                        Log.e("engine RPM", eRPM);
                        if (iRPM > 1500) {
                            mHandler.obtainMessage(6).sendToTarget();
                            //callRA.setVisibility(View.VISIBLE);
                            break;


                        }


                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                //End Code for RPM
*/
/*
                //Code for engine RPM command vs Mass air flow
                try {
                    RPMCommand rpmCommand1 = new RPMCommand();
                    Log.e("code works here","2nd func");
                    while (!Thread.currentThread().isInterrupted()) {
                        rpmCommand1.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        MassAirFlowCommand massAirFlowCommand = new MassAirFlowCommand();
                        String eRPM1 = rpmCommand1.getFormattedResult();
                        Log.e("2nd RPM", eRPM1);
                        eRPM1 = eRPM1.substring(0, eRPM1.length() - 3);
                        int iRPM1 = Integer.parseInt(eRPM1);


                        massAirFlowCommand.run(mmSocket.getInputStream(), mmSocket.getOutputStream());
                        Log.e("Mass AIr flow", massAirFlowCommand.getFormattedResult());
                        String massAirFlow = massAirFlowCommand.getFormattedResult();
                        massAirFlow = massAirFlow.substring(0,massAirFlow.length()-3);
                        float massAirFlowFloat = Float.parseFloat(massAirFlow);
                        if ( iRPM1 > 800 && iRPM1 <900)
                        {
                            if (massAirFlowFloat<2.55) {
                                mHandler.obtainMessage(5).sendToTarget();
                                //nearAuto.setVisibility(View.VISIBLE);
                                break;

                            }
                        }

                    }
                }catch (Exception e)
                {
                    e.printStackTrace();

                }

                //Code for engine RPM command vs Mass air flow
*/

            }catch (Exception e){
                e.printStackTrace();
                Log.e("problem with run","cannot read data from obd");

            }


            // Get the input and output streams, using temp objects because
            // member streams are final
            /*try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.e("Inside Try", "Inside Try of connected thread");

            } catch (IOException e) {
                Log.e("Inside Catch", "Inside Catch of connected thread");
            }
            Log.e("Outside Try", "Inside Try of connected thread");
            mmInStream = tmpIn;
          //  Log.e("Reading data from obd",mmInStream.toString());
            mmOutStream = tmpOut;
*/
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()
            Log.e("Inside run", "Inside run of connected thread");
            // Keep listening to the InputStream until an exception occurs
            /*while (true) {
                try {
                    // Read from the InputStream
                    Log.e("Before reading", "Inside while of connected thread");
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    Log.e("After reading", "Inside while of connected thread");
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }*/
        }


        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            /*try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }*/
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public class ModifiedTroubleCodesObdCommand extends TroubleCodesCommand {
        @Override
        public String getResult() {
            // remove unwanted response from output since this results in erroneous error codes
            return rawData.replace("SEARCHING...", "");
        }
    }

}

