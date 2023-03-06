package it.univaq.app.carapp.View;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.univaq.app.carapp.Model.Tracking;
import it.univaq.app.carapp.R;
import it.univaq.app.carapp.Utility.RoomDB.DB;
import it.univaq.app.carapp.Utility.Volley.RequestVolley;

public class listFragmentSessions extends Fragment {
    private List<Tracking> data = new ArrayList<>();
    MainAdapter adapter;
    RecyclerView recyclerView;
    ConnectivityManager connectivityManager;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                data = DB.getInstance(getContext()).getSessionDAO().findAll();
            }
        }).start();

        adapter = new MainAdapter(this.data);
        adapter.setOnSessionsAdapterListener(new MainAdapter.OnSessionAdapterListener() {
            @Override
            public void onOpenSession(Tracking session, int position) {
                //creare Bundle con dati, secondo argomento di:
                Navigation.findNavController(view)
                        .navigate(R.id.action_listFragmentSessions_to_detailFragmentSessions);
            }

            @Override
            public void onRemoveSession(Tracking session, int position) {
                //pannello di interazione per confermare o meno la cancellazione della sessione
                // dalla lista e dal DB locale/DB on line
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

            //Se c'è connessione richiedi i dati al server tramite get
            RequestVolley.getInstance(getContext()).doGetRequest("http://localhost/carapp.php?la_sede=forse",
                new RequestVolley.OnCompleteCallback() {
                    @Override
                    public void onCompleted(String response) {
                        //Log.v(StorageService.TAG, "Response from web: "+response);
                    }
                });
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };
}
