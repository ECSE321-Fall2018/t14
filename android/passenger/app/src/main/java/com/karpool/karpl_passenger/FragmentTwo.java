package com.karpool.karpl_passenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Displays the user's trips and allows them to open an individual trip as an instance of TripActivity
 */
public class FragmentTwo extends Fragment {


    // recycler views

    private List<Trip> upcomingTripsList = new ArrayList<>();
    private List<Trip> pastTripsList = new ArrayList<>();
    private RecyclerView upcomingRecyclerView, pastRecyclerView;
    private TripAdapter upcomingAdapter, pastAdapter;


    private TextView noPastTrips, noUpcomingTrips;


    // preference keys

    private final static String KEY_TRIP_DESTINATION = "tripdestination";
    private final static String KEY_TRIP_TIME = "time";
    private final static String KEY_TRIP_DATE = "date";
    private final static String KEY_TRIP_ORIGIN = "triplocation";
    private final static String KEY_TRIP_SEATS = "seats";
    private final static String KEY_TRIP_ID = "tripID";
    private final static String KEY_TRIP_DRIVER = "driver";
    private final static String KEY_USER_ID = "userID";
    private final static String KEY_TRIP_PRICE = "tripprice";
    private final static String KEY_TRIP_DRIVER_NUMBER = "number";
    private final static String KEY_TRIP_DRIVER_RATING = "rating";
    private final static String KEY_TRIP_STATUS = "tripJoined"; // if trip is already joined or if you are viewing a trip
    private final static String KEY_TRIP_FRAG_MODE = "tripMode"; // if a trip is upcoming or already happened

    private static String userID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_two, container, false);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userID = prefs.getString(KEY_USER_ID, null);


        // setting up the recycler views for upcoming/past trips

        upcomingRecyclerView = rootView.findViewById(R.id.myTripsRecyclerView);
        pastRecyclerView = rootView.findViewById(R.id.myOldTripsRecyclerView);


        noPastTrips = rootView.findViewById(R.id.noPastTrips);
        noUpcomingTrips = rootView.findViewById(R.id.noUpcomingTrips);

        upcomingAdapter = new TripAdapter(upcomingTripsList);
        pastAdapter = new TripAdapter(pastTripsList);

        RecyclerView.LayoutManager upcomingLayoutManager = new LinearLayoutManager(getActivity());
        upcomingRecyclerView.setLayoutManager(upcomingLayoutManager);
        upcomingRecyclerView.setItemAnimator(new DefaultItemAnimator());
        upcomingRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        upcomingRecyclerView.setAdapter(upcomingAdapter);

        RecyclerView.LayoutManager pastLayoutManager = new LinearLayoutManager(getActivity());
        pastRecyclerView.setLayoutManager(pastLayoutManager);
        pastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pastRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        pastRecyclerView.setAdapter(pastAdapter);


        // Click listener for trip selection
        upcomingRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), upcomingRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Trip trip = upcomingTripsList.get(position);
                prefs.edit().putString(KEY_TRIP_DATE, trip.getDate()).commit();
                prefs.edit().putString(KEY_TRIP_DESTINATION, trip.getDestination()).commit();
                prefs.edit().putString(KEY_TRIP_ORIGIN, trip.getOrigin()).commit();
                prefs.edit().putString(KEY_TRIP_TIME, trip.getTime()).commit();
                prefs.edit().putString(KEY_TRIP_DRIVER, trip.getDriver()).commit();
                prefs.edit().putString(KEY_TRIP_ID, trip.getTripID()).commit();
                prefs.edit().putString(KEY_TRIP_SEATS, trip.getSeats()).commit();
                prefs.edit().putString(KEY_TRIP_PRICE, trip.getPrice()).commit();
                prefs.edit().putString(KEY_TRIP_DRIVER_NUMBER, trip.getDriverNumber()).commit();
                prefs.edit().putString(KEY_TRIP_DRIVER_RATING, trip.getDriverRating()).commit();
                prefs.edit().putString(KEY_TRIP_FRAG_MODE, "UPCOMING").commit();
                prefs.edit().putString(KEY_TRIP_STATUS, "JOINED").commit();
                Intent I = new Intent(getActivity(), TripActivity.class);
                startActivity(I);
            }

            @Override
            public void onLongClick(View view, int position) {
                // do nothing
            }
        }));

        pastRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), pastRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Trip trip = pastTripsList.get(position);
                prefs.edit().putString(KEY_TRIP_DATE, trip.getDate()).commit();
                prefs.edit().putString(KEY_TRIP_DESTINATION, trip.getDestination()).commit();
                prefs.edit().putString(KEY_TRIP_ORIGIN, trip.getOrigin()).commit();
                prefs.edit().putString(KEY_TRIP_TIME, trip.getTime()).commit();
                prefs.edit().putString(KEY_TRIP_DRIVER, trip.getDriver()).commit();
                prefs.edit().putString(KEY_TRIP_ID, trip.getTripID()).commit();
                prefs.edit().putString(KEY_TRIP_SEATS, trip.getSeats()).commit();
                prefs.edit().putString(KEY_TRIP_PRICE, trip.getPrice()).commit();
                prefs.edit().putString(KEY_TRIP_DRIVER_RATING, trip.getDriverRating()).commit();
                prefs.edit().putString(KEY_TRIP_FRAG_MODE, "PAST").commit();
                prefs.edit().putString(KEY_TRIP_STATUS, "JOINED").commit();
                Intent I = new Intent(getActivity(), TripActivity.class);
                startActivity(I);
            }

            @Override
            public void onLongClick(View view, int position) {
                // do nothing
            }
        }));


        displayTrips();


        return rootView;
    }


    // If the user moves to the TripActivity and leaves the trip, it should not be listed in upcoming anymore, so a refresh is needed
    @Override
    public void onResume() {
        super.onResume();
        displayTrips();

    }


    /**
     * Displays the trap data for both upcoming and past trips. If the trips complete flag is set to true, it is added to past, otherwise, it is added to upcoming.
     */
    public void displayTrips() {

        HttpUtils.get("trips/passengers/" + userID, new RequestParams(), new JsonHttpResponseHandler() {

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {

                    upcomingTripsList.clear();
                    pastTripsList.clear();


                    // parse each trip in the passenger array and load it into the trip lists

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject obj = response.getJSONObject(i);
                        String date = obj.getString("departureDate");
                        String year = date.substring(0, 4);
                        String remainder = date.substring(4, 8);
                        String time = obj.getString("departureTime");
                        JSONObject driver = obj.getJSONObject("driver");
                        String driverName = driver.getString("name");
                        Boolean tripStatus = obj.getBoolean("tripComplete");
                        String driverNumber = driver.getString("phoneNumber");
                        JSONArray ratingArray = driver.getJSONArray("ratings");
                        // cities such as new york are entered into the database as new_york as the space cannot be parsed into a URL, this removes the underscore for user viewing
                        String origin = obj.getString("departureLocation").replaceAll("_", " ");
                        String destination = obj.getString("destination").replaceAll("_", " ");

                        // driver ratings are provided as an array so the average must be calculated

                        double driverRating = 0.0;

                        for (int ratingCount = 0; ratingCount < ratingArray.length(); ratingCount++) {
                            driverRating += ratingArray.getDouble(ratingCount);
                        }

                        driverRating /= ratingArray.length();


                        if (!tripStatus) {
                            upcomingTripsList.add(new Trip(origin, destination, year + "-" + formatter(remainder, "-", 2),
                                    formatter(time, ":", 2), driverName, driverNumber, Double.toString(driverRating), Integer.toString(obj.getInt("seatAvailable")), Integer.toString(obj.getInt("price")), Integer.toString(obj.getInt("tripId"))));
                        } else {
                            pastTripsList.add(new Trip(origin, destination, year + "-" + formatter(remainder, "-", 2),
                                    formatter(time, ":", 2), driverName, driverNumber, Double.toString(driverRating), Integer.toString(obj.getInt("seatAvailable")), Integer.toString(obj.getInt("price")), Integer.toString(obj.getInt("tripId"))));
                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (upcomingTripsList.isEmpty())
                    noUpcomingTrips.setVisibility(View.VISIBLE);
                else {
                    noUpcomingTrips.setVisibility(View.GONE);

                }


                if (pastTripsList.isEmpty())
                    noPastTrips.setVisibility(View.VISIBLE);
                else {
                    noPastTrips.setVisibility(View.GONE);

                }

                upcomingAdapter.notifyDataSetChanged();
                pastAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (upcomingTripsList.isEmpty())
                    noUpcomingTrips.setVisibility(View.VISIBLE);

                if (pastTripsList.isEmpty())
                    noPastTrips.setVisibility(View.VISIBLE);

                Toast.makeText(getActivity(), "There was a network error, try again later.", Toast.LENGTH_LONG).show(); // generic network error

            }


        });


    }

    /**
     * Used for date and time formatting
     *
     * @param text   the string you want to format
     * @param insert the character to insert
     * @param n      insert every n characters
     * @return
     */
    public static String formatter(
            String text, String insert, int n) {
        StringBuilder builder = new StringBuilder(
                text.length() + insert.length() * (text.length() / n) + 1);

        int index = 0;
        String prefix = "";
        while (index < text.length()) {

            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index,
                    Math.min(index + n, text.length())));
            index += n;
        }
        return builder.toString();
    }


}