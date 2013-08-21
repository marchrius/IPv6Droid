// (c) 2013 Dr. Andreas Feldner. See JAiccu license.

package de.flyingsnail.jaiccu;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

/**
 * Main activity as generated by Android Studio, plus Code to start the service when user clicks.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_START_VPN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Start the system-managed setup of VPN
     * @param view supplied by GUI invocation
     */
    public void startVPN (View view) {
        // Start system-managed intent for VPN
        Intent systemVpnIntent = VpnService.prepare(getApplicationContext());
        if (systemVpnIntent != null) {
            startActivityForResult(systemVpnIntent, REQUEST_START_VPN);
        } else {
            onActivityResult (REQUEST_START_VPN, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_START_VPN && resultCode == RESULT_OK) {
            /* @TODO should we query parameters in main activity and pass here
               as demonstrated in TopVpnClient, or should we query them in the
               configuration intent suggested by the VpnService API?
               Probably we should get here as far as setup username and password, and query
               available tunnels from the tic server and let the user select one.
             */
            Intent intent = new Intent(this, AiccuVpnService.class);
            startService(intent);
        }
    }


}