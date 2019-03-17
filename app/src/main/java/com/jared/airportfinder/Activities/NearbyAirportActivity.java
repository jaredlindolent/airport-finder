package com.jared.airportfinder.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jared.airportfinder.Classes.Flights;
import com.jared.airportfinder.Interfaces.TimetableListener;
import com.jared.airportfinder.R;
import com.jared.airportfinder.Sync.TimetableTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NearbyAirportActivity extends AppCompatActivity implements TimetableListener {

    String iataCode, airportName, timezone;

    private RecyclerView recycler;

    String result;
    JSONArray jsonArray;
    boolean getArrivalFlightData = false;

    ArrayList<HashMap<String, String>> cardsList;
    ArrayList<HashMap<String, String>> combined_cardsList;

    TextView airport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_airport);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        airportName = intent.getStringExtra("nameAirport");
        timezone = intent.getStringExtra("timezone");
        iataCode = intent.getStringExtra("codeIataAirport");
        combined_cardsList = new ArrayList<>();

        airport = findViewById(R.id.airportNameText);
        airport.setText(airportName);

        getAirportDepartTime(iataCode);
        getAirportArrivalTime(iataCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(NearbyAirportActivity.this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void getAirportDepartTime(String ic){
        TimetableTask timetableTask = new TimetableTask(NearbyAirportActivity.this, NearbyAirportActivity.this, ic);
        timetableTask.execute("http://aviation-edge.com/v2/public/timetable?key=350d9b-233c83&iataCode=" + ic + "&type=departure");
    }

    public void getAirportArrivalTime(String ic){
        TimetableTask timetableTask = new TimetableTask(NearbyAirportActivity.this, NearbyAirportActivity.this, ic);
        timetableTask.execute("http://aviation-edge.com/v2/public/timetable?key=350d9b-233c83&iataCode=" + ic + "&type=arrival");
    }

    public void manageTimeline(String result){
        try {
            jsonArray = new JSONArray(result);
            cardsList = jsonArrayToList(jsonArray);

            combined_cardsList.addAll(cardsList);
            getArrivalFlightData = true;
            Log.i("combined_cl", ""+ combined_cardsList.size());
            if (getArrivalFlightData) {
                ArrayList<Flights> _cardList = convertFlightCardList(combined_cardsList);
                setupAirportCards(_cardList);
            }
        } catch (JSONException e){
            e.printStackTrace();
            showAlertDialogWithSettings("Failed Airport timeline", "Could not get Nearby Airport timeline");
        }
    }

    /*
     * Alert dialog control
     */
    private void showAlertDialogWithSettings(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NearbyAirportActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(NearbyAirportActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        builder.show();

    }

    public ArrayList<HashMap<String, String>> jsonArrayToList(JSONArray flights) throws JSONException {
        ArrayList<HashMap<String, String>> pcl = new ArrayList<>();

        if (!getArrivalFlightData) {
            for (int i = 0; i < flights.length(); i++) {
                JSONObject p = flights.getJSONObject(i);
                String type = p.getString("type");
                String status = p.getString("status");
                JSONObject _depature = p.getJSONObject("departure");
                String scheduledTime = _depature.getString("scheduledTime");
                JSONObject _airline = p.getJSONObject("airline");
                String flightName = _airline.getString("name");
                JSONObject _flight = p.getJSONObject("flight");
                String flightNum = _flight.getString("number");

                HashMap<String, String> _planeCards = new HashMap<>();
                _planeCards.put("type", type);
                _planeCards.put("status", status);
                _planeCards.put("scheduledTime", scheduledTime);
                _planeCards.put("flightName", flightName);
                _planeCards.put("flightNum", flightNum);

                pcl.add(_planeCards);
            }
        } else {
            for (int i = 0; i < flights.length(); i++) {
                JSONObject p = flights.getJSONObject(i);
                String type = p.getString("type");
                String status = p.getString("status");
                JSONObject _depature = p.getJSONObject("arrival");
                String scheduledTime = _depature.getString("scheduledTime");
                JSONObject _airline = p.getJSONObject("airline");
                String flightName = _airline.getString("name");
                JSONObject _flight = p.getJSONObject("flight");
                String flightNum = _flight.getString("number");

                HashMap<String, String> _planeCards = new HashMap<>();
                _planeCards.put("type", type);
                _planeCards.put("status", status);
                _planeCards.put("scheduledTime", scheduledTime);
                _planeCards.put("flightName", flightName);
                _planeCards.put("flightNum", flightNum);

                pcl.add(_planeCards);
            }
        }
        return pcl;
    }

    ArrayList<Flights> convertFlightCardList(ArrayList<HashMap<String, String>> cardsList){
        ArrayList<Flights> cList = new ArrayList<>();

        for (Map<String, String> map : cardsList){
            String type = map.get("type");
            String status = map.get("status");
            String scheduledTime = map.get("scheduledTime");
            String flightName = map.get("flightName");
            String flightNum = map.get("flightNum");

            Flights flight = new Flights(type, status,scheduledTime, flightName, flightNum);
            cList.add(flight);
        }
        return cList;
    }

    public void setupAirportCards(ArrayList<Flights> cList) {
        recycler = findViewById(R.id.airportCards);
        recycler.setAdapter(new FlightsAdapter(this, cList));
        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onTimeTableListener(String flights) {
        if (flights != ""){
            result = "";
            result = flights;
            Log.i("flights", result);
            manageTimeline(result);
        }
    }
}

class FlightsAdapter<T> extends RecyclerView.Adapter<FlightsAdapter.ViewHolder>{
    public ArrayList<Flights> items;
    private Context context;

    public FlightsAdapter(Context context, ArrayList<Flights> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.plane_card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlightsAdapter.ViewHolder viewHolder, int i) {
        viewHolder.airplaneName.setText(items.get(i).getFlightName());

        if (items.get(i).getType().equals("departure")){
            viewHolder.statusIcon.setImageResource(R.drawable.ic_dot_red_24dp);
        } else {
            viewHolder.statusIcon.setImageResource(R.drawable.ic_green_dot_24dp);
        }
        viewHolder.status.setText(items.get(i).getStatus());

        String time = items.get(i).getDepartTime().substring(11,16);
        viewHolder.departureTime.setText(time);
        viewHolder.flightNumber.setText(items.get(i).getFlightNum());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView airplaneName, status, departureTime, flightNumber;
        ImageView statusIcon;

        public ViewHolder(@NonNull View inflate) {
            super(inflate);
            statusIcon = inflate.findViewById(R.id.statusIcon);
            airplaneName = inflate.findViewById(R.id.airlineName);
            status = inflate.findViewById(R.id.statusText);
            departureTime = inflate.findViewById(R.id.depTimeText);
            flightNumber = inflate.findViewById(R.id.flightNumText);

        }
    }

}
