/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.nicidienase.chaosflix.fragments;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import de.nicidienase.chaosflix.CardPresenter;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.activities.BrowseErrorActivity;
import de.nicidienase.chaosflix.activities.DetailsActivity;
import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Movie;
import de.nicidienase.chaosflix.entities.MovieList;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends BrowseFragment {
	private static final String TAG = "MainFragment";

	private static final int BACKGROUND_UPDATE_DELAY = 300;
	private static final int GRID_ITEM_WIDTH = 200;
	private static final int GRID_ITEM_HEIGHT = 200;
	private static final int NUM_ROWS = 6;
	private static final int NUM_COLS = 15;

	private final Handler mHandler = new Handler();
	private ArrayObjectAdapter mRowsAdapter;
	private Drawable mDefaultBackground;
	private DisplayMetrics mMetrics;
	private Timer mBackgroundTimer;
	private URI mBackgroundURI;
	private BackgroundManager mBackgroundManager;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onActivityCreated(savedInstanceState);

		prepareBackgroundManager();

		setupUIElements();

		loadRows();

		setupEventListeners();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundTimer) {
			Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
			mBackgroundTimer.cancel();
		}
	}

	private void loadRows() {
		new MediaCCCClient().getConference(101).enqueue(new Callback<Conference>() {
			@Override
			public void onResponse(Call<Conference> call, Response<Conference> response) {
				Conference conference = response.body();
				HashMap<String, List<Event>> eventsByTags = conference.getEventsByTags();

				mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
				CardPresenter cardPresenter = new CardPresenter();

				List<Event> other = new LinkedList<Event>();
				for (String tag: eventsByTags.keySet()) {
					if(android.text.TextUtils.isDigitsOnly(tag)){
						other.addAll(eventsByTags.get(tag));
					} else {
						ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
						listRowAdapter.addAll(0,eventsByTags.get(tag));
						HeaderItem header = new HeaderItem(tag);
						mRowsAdapter.add(new ListRow(header, listRowAdapter));
					}
				}
				if(other.size() > 0){
					ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
					listRowAdapter.addAll(0,other);
					HeaderItem header = new HeaderItem("other");
					mRowsAdapter.add(new ListRow(header,listRowAdapter));
				}

				setAdapter(mRowsAdapter);
			}

			@Override
			public void onFailure(Call<Conference> call, Throwable t) {

			}
		});
	}

	private void prepareBackgroundManager() {

		mBackgroundManager = BackgroundManager.getInstance(getActivity());
		mBackgroundManager.attach(getActivity().getWindow());
		mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	private void setupUIElements() {
		// setBadgeDrawable(getActivity().getResources().getDrawable(
		// R.drawable.videos_by_google_banner));
		setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
		// over title
		setHeadersState(HEADERS_ENABLED);
		setHeadersTransitionOnBackEnabled(true);

		// set fastLane (or headers) background color
		setBrandColor(getResources().getColor(R.color.black_opaque));
		// set search icon color
		setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));

	}

	private void setupEventListeners() {
		setOnItemViewClickedListener(new ItemViewClickedListener());
		setOnItemViewSelectedListener(new ItemViewSelectedListener());
	}

	protected void updateBackground(String uri) {
		int width = mMetrics.widthPixels;
		int height = mMetrics.heightPixels;
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.error(mDefaultBackground)
				.into(new SimpleTarget<GlideDrawable>(width, height) {
					@Override
					public void onResourceReady(GlideDrawable resource,
												GlideAnimation<? super GlideDrawable>
														glideAnimation) {
						mBackgroundManager.setDrawable(resource);
					}
				});
		mBackgroundTimer.cancel();
	}

	private void startBackgroundTimer() {
		if (null != mBackgroundTimer) {
			mBackgroundTimer.cancel();
		}
		mBackgroundTimer = new Timer();
		mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
	}

	private final class ItemViewClickedListener implements OnItemViewClickedListener {
		@Override
		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
								  RowPresenter.ViewHolder rowViewHolder, Row row) {

			Event event = (Event) item;
			Intent i = new Intent(getActivity(), DetailsActivity.class);
			i.putExtra(DetailsActivity.EVENT,event);
			Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					getActivity(),
					((ImageCardView) itemViewHolder.view).getMainImageView(),
					DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
			getActivity().startActivity(i, bundle);
		}
	}

	private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
		@Override
		public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
								   RowPresenter.ViewHolder rowViewHolder, Row row) {
			if (item instanceof Movie) {
				mBackgroundURI = ((Movie) item).getBackgroundImageURI();
				startBackgroundTimer();
			}

		}
	}

	private class UpdateBackgroundTask extends TimerTask {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mBackgroundURI != null) {
						updateBackground(mBackgroundURI.toString());
					}
				}
			});

		}
	}

	private class GridItemPresenter extends Presenter {
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent) {
			TextView view = new TextView(parent.getContext());
			view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setBackgroundColor(getResources().getColor(R.color.default_background));
			view.setTextColor(Color.WHITE);
			view.setGravity(Gravity.CENTER);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, Object item) {
			((TextView) viewHolder.view).setText((String) item);
		}

		@Override
		public void onUnbindViewHolder(ViewHolder viewHolder) {
		}
	}

}