package it.univaq.app.carapp.View;

import android.os.Bundle;
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

import it.univaq.app.carapp.Model.Session;
import it.univaq.app.carapp.R;

public class listFragmentSessions extends Fragment {
    private List<Session> data = new ArrayList<>();
    MainAdapter adapter;
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Session session1 = new Session();
        data.add(session1);
        Session session2 = new Session();
        data.add(session2);

        System.out.println("ARRIVA FIN QUI LA SEDE");

        adapter = new MainAdapter(this.data);
        adapter.setOnSessionsAdapterListener(new MainAdapter.OnSessionAdapterListener() {
            @Override
            public void onOpenSession(Session session, int position) {
                //creare Bundle con dati, secondo argomento di:
                Navigation.findNavController(view)
                        .navigate(R.id.action_listFragmentSessions_to_detailFragmentSessions);
            }

            @Override
            public void onRemoveSession(Session session, int position) {
                //pannello di interazione per confermare o meno la cancellazione della sessione
                // dalla lista e dal DB locale/DB on line
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}
