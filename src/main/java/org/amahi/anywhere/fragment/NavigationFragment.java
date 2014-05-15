package org.amahi.anywhere.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerSharesAdapter;
import org.amahi.anywhere.adapter.ServersAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import javax.inject.Inject;

public class NavigationFragment extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener
{
	public static NavigationFragment newInstance() {
		return new NavigationFragment();
	}

	@Inject
	AmahiClient amahiClient;

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_navigation, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpServers();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpServers() {
		setUpServersAdapter();
		setUpServersContent();
		setUpServersListener();
	}

	private void setUpServersAdapter() {
		getServersSpinner().setAdapter(new ServersAdapter(getActivity()));
	}

	private Spinner getServersSpinner() {
		return (Spinner) getView().findViewById(R.id.spinner_servers);
	}

	private void setUpServersContent() {
		amahiClient.getServers("TOKEN");
	}

	@Subscribe
	public void onServersLoaded(ServersLoadedEvent event) {
		setUpServersContent(event.getServers());

		showContent();
	}

	private void setUpServersContent(List<Server> servers) {
		getServersAdapter().replaceWith(servers);
	}

	private ServersAdapter getServersAdapter() {
		return (ServersAdapter) getServersSpinner().getAdapter();
	}

	private void showContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_content);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.layout_content)));
	}

	private void setUpServersListener() {
		getServersSpinner().setOnItemSelectedListener(this);
	}

	@Override
	public void onNothingSelected(AdapterView<?> spinnerView) {
	}

	@Override
	public void onItemSelected(AdapterView<?> spinnerView, View view, int position, long id) {
		Server server = getServersAdapter().getItem(position);

		setUpShares(server);
	}

	private void setUpShares(Server server) {
		setUpSharesAdapter();
		setUpSharesListener();

		setUpServerConnection(server);
	}

	private void setUpSharesAdapter() {
		getSharesList().setAdapter(new ServerSharesAdapter(getActivity()));
	}

	private ListView getSharesList() {
		return (ListView) getView().findViewById(R.id.list_shares);
	}

	private void setUpServerConnection(Server server) {
		if (!serverClient.isConnected(server)) {
			serverClient.connect(server);
		} else {
			setUpSharesContent();
		}
	}

	@Subscribe
	public void onServerConnected(ServerConnectedEvent event) {
		setUpSharesContent();
	}

	private void setUpSharesContent() {
		serverClient.getShares();
	}

	@Subscribe
	public void onSharesLoaded(ServerSharesLoadedEvent event) {
		setUpSharesContent(event.getServerShares());

		showSharesContent();
	}

	private void setUpSharesContent(List<ServerShare> shares) {
		getSharesAdapter().replaceWith(shares);
	}

	private ServerSharesAdapter getSharesAdapter() {
		return (ServerSharesAdapter) getSharesList().getAdapter();
	}

	private void showSharesContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_shares);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.list_shares)));
	}

	private void setUpSharesListener() {
		getSharesList().setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		getSharesList().setItemChecked(position, true);
	}

	@Override
	public void onResume() {
		super.onResume();

		BusProvider.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		BusProvider.getBus().unregister(this);
	}
}
