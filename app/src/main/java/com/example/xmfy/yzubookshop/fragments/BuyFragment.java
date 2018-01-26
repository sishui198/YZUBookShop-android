package com.example.xmfy.yzubookshop.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.xmfy.yzubookshop.R;
import com.example.xmfy.yzubookshop.model.Book;
import com.example.xmfy.yzubookshop.module.buy.BaseBuyFragment;
import com.example.xmfy.yzubookshop.module.buy.BookSearchHelper;
import com.example.xmfy.yzubookshop.module.buy.BookSuggestion;
import com.example.xmfy.yzubookshop.module.buy.SearchResultsListAdapter;

import java.util.List;

/**
 * Created by xmfy on 2018/1/3.
 */
public class BuyFragment extends BaseBuyFragment {

    private static final long ANIM_DURATION = 350;
    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    private FloatingSearchView mSearchView;
    private RecyclerView mSearchResultsList;
    private SearchResultsListAdapter mSearchResultsAdapter;

    private String mLastQuery = "";

    public BuyFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buy, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchView = view.findViewById(R.id.floating_search_view);
        mSearchResultsList = view.findViewById(R.id.search_results_list);
        setupFloatingSearch();
        setupResultsList();
        setupDrawer();
    }

    private void setupFloatingSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")){
                    mSearchView.clearSuggestions();
                }else {
                    mSearchView.showProgress();
                    BookSearchHelper.findSuggestions(getActivity(), newQuery, 5,
                            FIND_SUGGESTION_SIMULATED_DELAY, new BookSearchHelper.OnFindSuggestionsListener() {
                                @Override
                                public void onResults(List<BookSuggestion> results) {
                                    mSearchView.swapSuggestions(results);
                                    mSearchView.hideProgress();
                                }
                            });
                }
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                BookSuggestion bookSuggestion = (BookSuggestion) searchSuggestion;
                BookSearchHelper.findBooks(getActivity(), bookSuggestion.getBody(),
                        new BookSearchHelper.OnFindColorsListener() {
                            @Override
                            public void onResults(List<Book> results) {
                                Log.e("fragment", results.toString());
                                mSearchResultsAdapter.swapData(results);
                            }
                        });
                mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;
                BookSearchHelper.findBooks(getActivity(), query,
                        new BookSearchHelper.OnFindColorsListener() {
                            @Override
                            public void onResults(List<Book> results) {
                                mSearchResultsAdapter.swapData(results);
                            }
                        });
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mSearchView.swapSuggestions(BookSearchHelper.getHistory(getActivity(), 3));
            }

            @Override
            public void onFocusCleared() {
                mSearchView.setSearchBarTitle(mLastQuery);
            }
        });

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_search){
                    Log.e("fragment", "action search");
                }
            }
        });

        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                Log.e("fragment", "onBindSuggestion");
            }
        });

        mSearchView.setOnSuggestionsListHeightChanged(new FloatingSearchView.OnSuggestionsListHeightChanged() {
            @Override
            public void onSuggestionsListHeightChanged(float newHeight) {
                mSearchResultsList.setTranslationY(newHeight);
            }
        });

        mSearchView.setOnClearSearchActionListener(new FloatingSearchView.OnClearSearchActionListener() {
            @Override
            public void onClearSearchClicked() {

                Log.d("fragment", "onClearSearchClicked()");
            }
        });


    }

    private void setupResultsList() {
        mSearchResultsAdapter = new SearchResultsListAdapter();
        mSearchResultsList.setAdapter(mSearchResultsAdapter);
        mSearchResultsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public boolean onActivityBackPress() {
        return mSearchView.setSearchFocused(false);
    }

    private void setupDrawer() {
        attachSearchViewActivityDrawer(mSearchView);
    }
}
