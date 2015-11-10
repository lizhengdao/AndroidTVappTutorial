package com.corochann.androidtvapptutorial.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import com.corochann.androidtvapptutorial.data.VideoItemLoader;
import com.corochann.androidtvapptutorial.model.Movie;
import com.corochann.androidtvapptutorial.data.MovieProvider;
import com.corochann.androidtvapptutorial.ui.background.PicassoBackgroundManager;
import com.corochann.androidtvapptutorial.R;
import com.corochann.androidtvapptutorial.common.Utils;
import com.corochann.androidtvapptutorial.ui.presenter.CardPresenter;
import com.corochann.androidtvapptutorial.ui.presenter.CustomFullWidthDetailsOverviewRowPresenter;
import com.corochann.androidtvapptutorial.ui.presenter.DetailsDescriptionPresenter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by corochann on 6/7/2015.
 */
public class VideoDetailsFragment extends DetailsFragment {

    private static final String TAG = VideoDetailsFragment.class.getSimpleName();

    private static final int ACTION_PLAY_VIDEO = 1;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int LOADER_ID = 2;

    private static final String MOVIE = "Movie";

    private CustomFullWidthDetailsOverviewRowPresenter mFwdorPresenter;
    private PicassoBackgroundManager mPicassoBackgroundManager;

    private Movie mSelectedMovie;
    private DetailsRowBuilderTask mDetailsRowBuilderTask;
    private boolean isBuilderTaskDone = false;
    private boolean isLoadFinished = false;

    private ArrayObjectAdapter mAdapter;
    private ListRow mRelatedVideoRow = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mFwdorPresenter = new CustomFullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());

        mPicassoBackgroundManager = new PicassoBackgroundManager(getActivity());
        mSelectedMovie = (Movie)getActivity().getIntent().getSerializableExtra(MOVIE);

        mDetailsRowBuilderTask = (DetailsRowBuilderTask) new DetailsRowBuilderTask().execute(mSelectedMovie);


        mPicassoBackgroundManager.updateBackgroundWithDelay(mSelectedMovie.getCardImageUrl());
        getLoaderManager().initLoader(VideoItemLoader.VIDEO_ITEM_LOADER_ID, null, new VideoDetailsFragmentLoaderCallbacks());
    }

    @Override
    public void onStop() {
        mDetailsRowBuilderTask.cancel(true);
        super.onStop();
    }

    private class VideoDetailsFragmentLoaderCallbacks implements LoaderManager.LoaderCallbacks<LinkedHashMap<String, List<Movie>>> {
        @Override
        public Loader<LinkedHashMap<String, List<Movie>>> onCreateLoader(int id, Bundle args) {
            /* Create new Loader */
            Log.d(TAG, "onCreateLoader");
            if(id == VideoItemLoader.VIDEO_ITEM_LOADER_ID) {
                Log.d(TAG, "create VideoItemLoader");
                return new VideoItemLoader(getActivity());
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<LinkedHashMap<String, List<Movie>>> loader, LinkedHashMap<String, List<Movie>> data) {
            Log.d(TAG, "onLoadFinished");
            /* Loader data has prepared. Start updating UI here */
            switch (loader.getId()) {
                case VideoItemLoader.VIDEO_ITEM_LOADER_ID:
                    Log.d(TAG, "VideoLists UI update");

                    //mAdapter = new ArrayObjectAdapter(new ListRowPresenter());

                    int index = 0;

                    /* CardPresenter */
                    CardPresenter cardPresenter = new CardPresenter();

                    if (null != data) {
                        for (Map.Entry<String, List<Movie>> entry : data.entrySet()) {
                            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
                            List<Movie> list = entry.getValue();

                            for (int j = 0; j < list.size(); j++) {
                                cardRowAdapter.add(list.get(j));
                            }
                            //HeaderItem header = new HeaderItem(index, entry.getKey());
                            HeaderItem header = new HeaderItem(0, "Related Videos");
                            index++;

                            mRelatedVideoRow = new ListRow(header, cardRowAdapter);
                            if(isBuilderTaskDone){
                                /* Set */
                                mAdapter.add(mRelatedVideoRow);
                                setAdapter(mAdapter);
                            }
                        }
                    } else {
                        Log.e(TAG, "An error occurred fetching videos");
                    }
                    isLoadFinished = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<LinkedHashMap<String, List<Movie>>> loader) {
            Log.d(TAG, "onLoadReset");
            /* When it is called, Loader data is now unavailable due to some reason. */

        }
    }


    private class DetailsRowBuilderTask extends AsyncTask<Movie, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(Movie... params) {
            Log.v(TAG, "DetailsRowBuilderTask doInBackground");
            DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
            try {
                // Bitmap loading must be done in background thread in Android.
                Bitmap poster = Picasso.with(getActivity())
                        .load(mSelectedMovie.getCardImageUrl())
                        .resize(Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH),
                                Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT))
                        .centerCrop()
                        .get();
                row.setImageBitmap(getActivity(), poster);
            } catch (IOException e) {
                Log.w(TAG, e.toString());
            }


            return row;
        }

        @Override
        protected void onPostExecute(DetailsOverviewRow row) {
            Log.v(TAG, "DetailsRowBuilderTask onPostExecute");
            /* 1st row: DetailsOverviewRow */

              /* action setting*/
            SparseArrayObjectAdapter sparseArrayObjectAdapter = new SparseArrayObjectAdapter();
            sparseArrayObjectAdapter.set(0, new Action(ACTION_PLAY_VIDEO, "Play Video"));
            sparseArrayObjectAdapter.set(1, new Action(1, "Action 2", "label"));
            sparseArrayObjectAdapter.set(2, new Action(2, "Action 3", "label"));

            row.setActionsAdapter(sparseArrayObjectAdapter);

            mFwdorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
                @Override
                public void onActionClicked(Action action) {
                    if (action.getId() == ACTION_PLAY_VIDEO) {
                        Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                        intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
                        intent.putExtra(getResources().getString(R.string.should_start), true);
                        startActivity(intent);
                    }
                }
            });


            /* 2nd row: ListRow */
/*            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());

            ArrayList<Movie> mItems = MovieProvider.getMovieItems();
            for (Movie movie : mItems) {
                listRowAdapter.add(movie);
            }
            HeaderItem headerItem = new HeaderItem(0, "Related Videos");*/

            ClassPresenterSelector classPresenterSelector = new ClassPresenterSelector();
            Log.v(TAG, "mFwdorPresenter.getInitialState: " + mFwdorPresenter.getInitialState());

            classPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mFwdorPresenter);
            classPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());


            mAdapter = new ArrayObjectAdapter(classPresenterSelector);
            /* 1st row */
            mAdapter.add(row);
            /* 2nd row */
            //mAdapter.add(new ListRow(headerItem, listRowAdapter));
            if(isLoadFinished){
                if(mRelatedVideoRow != null) {
                    mAdapter.add(mRelatedVideoRow);
                }
            }
            /* 3rd row */
            //adapter.add(new ListRow(headerItem, listRowAdapter));
            setAdapter(mAdapter);
            isBuilderTaskDone = true;
        }
    }
}