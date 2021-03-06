package de.nicidienase.chaosflix.touch.browse.eventslist;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent;
import de.nicidienase.chaosflix.databinding.FragmentEventsListBinding;
import de.nicidienase.chaosflix.touch.OnEventSelectedListener;
import de.nicidienase.chaosflix.touch.browse.BrowseFragment;
import de.nicidienase.chaosflix.touch.browse.adapters.EventRecyclerViewAdapter;

public class EventsListFragment extends BrowseFragment implements SearchView.OnQueryTextListener {

	private static final String ARG_COLUMN_COUNT    = "column-count";
	private static final String ARG_CONFERENCE      = "conference";
	private static final String LAYOUTMANAGER_STATE = "layoutmanager-state";
	private static final String TAG                 = EventsListFragment.class.getSimpleName();
	public static final  long   BOOKMARKS_LIST_ID   = -1;
	public static final  long   IN_PROGRESS_LIST_ID = -2;

	private int columnCount = 1;
	private OnEventSelectedListener listener;

	private EventRecyclerViewAdapter eventAdapter;
	private long                     conferenceId;

	private LinearLayoutManager       layoutManager;
	private SearchView                searchView;
	private FragmentEventsListBinding binding;

	public static EventsListFragment newInstance(long conferenceId, int columnCount) {
		EventsListFragment fragment = new EventsListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		args.putLong(ARG_CONFERENCE, conferenceId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		setHasOptionsMenu(true);
		if (context instanceof OnEventSelectedListener) {
			listener = (OnEventSelectedListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
			conferenceId = getArguments().getLong(ARG_CONFERENCE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentEventsListBinding.inflate(inflater, container, false);

		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setSupportActionBar(binding.incToolbar.toolbar);
		setOverlay(binding.incOverlay.loadingOverlay);

		if (columnCount <= 1) {
			layoutManager = new LinearLayoutManager(getContext());
		} else {
			layoutManager = new GridLayoutManager(getContext(), columnCount);
		}
		binding.list.setLayoutManager(layoutManager);

		eventAdapter = new EventRecyclerViewAdapter(listener);
		binding.list.setAdapter(eventAdapter);

		Observer<List<PersistentEvent>> listObserver = persistentEvents -> {
			setEvents(persistentEvents);
			if (persistentEvents.size() > 0) {
				setLoadingOverlayVisibility(false);
			}
		};

		Resources resources = getResources();
		if (conferenceId == BOOKMARKS_LIST_ID) {
			setupToolbar(binding.incToolbar.toolbar, R.string.bookmarks);
			getViewModel().getBookmarkedEvents().observe(this, listObserver);
			setLoadingOverlayVisibility(false);
		} else if (conferenceId == IN_PROGRESS_LIST_ID) {
			setupToolbar(binding.incToolbar.toolbar, R.string.continue_watching);
			getViewModel().getInProgressEvents().observe(this, listObserver);
			setLoadingOverlayVisibility(false);
		} else {
			{
				getViewModel().getConference(conferenceId).observe(this, conference -> {
					if (conference != null) {
						setupToolbar(binding.incToolbar.toolbar, conference.getTitle(), false);
						eventAdapter.setShowTags(conference.getTagsUsefull());
					}
				});

				getViewModel().getEventsforConference(conferenceId).observe(this, listObserver);
				getViewModel().updateEventsForConference(conferenceId).observe(this, loadingFinished -> setLoadingOverlayVisibility(!loadingFinished));
			}
		}
		return binding.getRoot();
	}

	private void setEvents(List<PersistentEvent> persistentEvents) {
		eventAdapter.setItems(persistentEvents);

		Parcelable layoutState = getArguments().getParcelable(LAYOUTMANAGER_STATE);
		if (layoutState != null) { layoutManager.onRestoreInstanceState(layoutState); }
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putParcelable(LAYOUTMANAGER_STATE, layoutManager.onSaveInstanceState());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.events_menu, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchView = (SearchView) searchMenuItem.getActionView();
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

		searchView.setSearchableInfo(searchManager.
				                                          getSearchableInfo(getActivity().getComponentName()));
		searchView.setSubmitButtonEnabled(true);
		searchView.setIconified(false);
		searchView.setOnQueryTextListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		eventAdapter.getFilter().filter(newText);
		return true;
	}
}
