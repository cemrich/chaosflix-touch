package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.ConferenceGroup;
import de.nicidienase.chaosflix.touch.adapters.ConferenceRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ConferenceGroupFragment extends ChaosflixFragment {

	private static final String TAG = ConferenceGroupFragment.class.getSimpleName();

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String ARG_GROUP = "group-name";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener listener;

	private int columnCount = 1;
	private ConferenceGroup conferenceGroup;

	private ConferenceRecyclerViewAdapter conferencesAdapter;

	CompositeDisposable disposable = new CompositeDisposable();
	private RecyclerView.LayoutManager layoutManager;

	public ConferenceGroupFragment() {
	}

	public static ConferenceGroupFragment newInstance(ConferenceGroup group, int columnCount) {
		ConferenceGroupFragment fragment = new ConferenceGroupFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		args.putParcelable(ARG_GROUP, group);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			conferenceGroup = getArguments().getParcelable(ARG_GROUP);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.recycler_view_pager_layout, container, false);

		if (view instanceof RecyclerView) {
			Context context = view.getContext();
			RecyclerView recyclerView = (RecyclerView) view;
			if (columnCount <= 1) {
				layoutManager = new LinearLayoutManager(context);
			} else {
				layoutManager = new GridLayoutManager(context, columnCount);
			}
			recyclerView.setLayoutManager(layoutManager);

			conferencesAdapter = new ConferenceRecyclerViewAdapter(listener);
			recyclerView.setAdapter(conferencesAdapter);
			disposable.add(getViewModel().getConferencesByGroup(conferenceGroup.getConferenceGroupId())
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(conferenceList -> conferencesAdapter.setItems(conferenceList)));
		}
		Parcelable layoutState = getArguments().getParcelable(LAYOUTMANAGER_STATE);
		if (layoutState != null) {
			layoutManager.onRestoreInstanceState(layoutState);
		}
		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener) {
			listener = (ConferencesTabBrowseFragment.OnConferenceListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(LAYOUTMANAGER_STATE, layoutManager.onSaveInstanceState());
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putParcelable(LAYOUTMANAGER_STATE, layoutManager.onSaveInstanceState());
	}

	@Override
	public void onStop() {
		super.onStop();
		disposable.clear();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
}
