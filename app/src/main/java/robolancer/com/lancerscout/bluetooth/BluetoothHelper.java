package robolancer.com.lancerscout.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import robolancer.com.lancerscout.R;
import robolancer.com.lancerscout.activities.teams.MatchScoutingActivity;

public class BluetoothHelper implements Runnable{

    private OutputStream outputStream;
    private InputStream inStream;

    private byte[] byteCommand;
    private String command;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothHelper(Context context, BluetoothAdapter bluetoothAdapter){
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void showBluetoothDevices(){
        //AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
        ArrayList<String> deviceName = new ArrayList<>();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

                if(bondedDevices.size() > 0) {
                    deviceList.addAll(bondedDevices);
                }
            }
        }

        for(BluetoothDevice device : deviceList){
            deviceName.add(device.getName());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(deviceName);

        new AlertDialog.Builder(context).setAdapter(arrayAdapter, (dialog, which) -> {
            try {
                dialog.dismiss();
                BluetoothDevice device = deviceList.get(which);
                ParcelUuid uuid = new ParcelUuid(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ed" + ""));
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid.getUuid());
                //bluetoothAdapter.cancelDiscovery();
                Toast.makeText(context, "Connecting to " + device.getName(), Toast.LENGTH_LONG).show();
                socket.connect();
                Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                inStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(context, "Something went wrong with bluetooth", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }).show();
    }

    @Override
    public void run(){
        try {
            while (true) {
                if (inStream != null) {
                    byteCommand = readByteArrayCommand(inStream);
                    command = new String(byteCommand);

                    if(command.equals("")){
                        Log.e("BluetoothHelper", "Exit Command Received. Finished");
                        break;
                    }

                    processCommand(command);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void processCommand(String command) {
        switch (command) {
            case "ADDTEAM":
                Log.e("BluetoothHelper", "ADDED TEAM");
                break;
            default:
                Log.e("BluetoothHelper", "Received " + command);
        }
    }

    public void write(int command) throws IOException {
        if(outputStream != null) {
            outputStream.write(command);
        }
    }

    public void write(String command) throws IOException {
        if(outputStream != null) {
            if (command != null && !command.isEmpty()) {
                outputStream.write("MATCH".getBytes());
                outputStream.write(0);
                outputStream.write(command.getBytes());
                outputStream.write(0);
            }
        }
    }

    private byte[] readByteArrayCommand(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read = inputStream.read();

        while(read != -1 && read != 0){
            byteArrayOutputStream.write(read);
            read = inputStream.read();
        }

        return byteArrayOutputStream.toByteArray();
    }


}