package it.univaq.app.carapp.View.Fragment;

import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.app.carapp.R;
import it.univaq.app.carapp.Service.DataLayerListenerService;
import it.univaq.app.carapp.Utility.Volley.RequestVolley;
import it.univaq.app.carapp.View.MainAdapter;

public class ListFragment extends Fragment {
    private List<String> data = new ArrayList<>();
    private MainAdapter adapter;
    private RecyclerView recyclerView;
    private ConnectivityManager connectivityManager;

    public static final String KEY_EXTRA_DATA = "extra_data";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager = (ConnectivityManager) requireContext().getSystemService(ConnectivityManager.class);

        connectivityManager.requestNetwork(networkRequest, networkCallback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MainAdapter(this.data);
        adapter.setOnSessionsAdapterListener(new MainAdapter.OnSessionAdapterListener() {
            @Override
            public void onOpenSession(String string, int position) {
                Bundle bundle = new Bundle();
                bundle.putString(ListFragment.KEY_EXTRA_DATA, data.get(position));
                Navigation.findNavController(view)
                        .navigate(R.id.action_listFragmentSessions_to_detailFragmentSessions, bundle);
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            String Url = "http://"+RequestVolley.ID_HOST_CARAPP+"/carapp.php?action=query&days=true";
            RequestVolley.getInstance(getContext()).doGetRequest(Url,
                new RequestVolley.OnCompleteCallback() {
                    @Override
                    public void onCompleted(String response) {
                        if(response!=null){
                            //Log.v(DataLayerListenerService.TAG, "Data from web "+response);
                            try{
                                JSONObject jsonObj = new JSONObject(response);
                                for (int i = 0; i < jsonObj.names().length(); i++){
                                    data.add(jsonObj.get(jsonObj.names().getString(i)).toString());
                                }
                                adapter.notifyDataSetChanged();
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                        else{
                            Log.v(DataLayerListenerService.TAG, "Dati null ");

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.communication_problems)
                                    .setTitle(R.string.attention).setPositiveButton("OK",null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);

            Toast.makeText(getActivity(), R.string.connection_lost, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };

}
