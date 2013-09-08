package com.mg_movie.activity;

import java.util.ArrayList;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.mg_movie.AppLog;
import com.mg_movie.MG_Exit;
import com.mg_movie.R;
import com.mg_movie.adapter.MG_MediaAdapter;
import com.mg_movie.imageloader.AbsListViewBaseActivity;
import com.mg_movie.player.JieLiveVideoPlayer;
import com.mg_movie.type.Type_v_qq_com;
import com.mg_movie.utils.DBUtils;
import com.yixia.vparser.VParser;
import com.yixia.vparser.model.Video;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MG_MOVIE extends AbsListViewBaseActivity {

	private PullToRefreshGridView movieRefresh;
	public MG_MOVIE instance;
	MG_MediaAdapter adapter;
	VParser vParser;
	public ArrayList<String> pages;
	DBUtils dbUtils;
	public List<Type_v_qq_com> list_main = new ArrayList<Type_v_qq_com>();
	public int index = 0;
	public int count = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mg__movie);
		instance = this;
		MG_Exit.getInstance().addActivity(this);
		dbUtils = new DBUtils(this);
		int temp_count = dbUtils.getMovieCount();
		if ((temp_count % 100) == 0) {
			count = temp_count / 100;
		}else {
			count = temp_count / 100 + 1;
		}
		movieRefresh = (PullToRefreshGridView)findViewById(R.id.pull_refresh_grid);
		movieRefresh.setMode(Mode.PULL_FROM_END);
		listView = movieRefresh.getRefreshableView();
		movieRefresh.setOnRefreshListener(new OnRefreshListener<GridView>() {
			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				if (index < count) {
					index++;
					new InitData().execute();
				}else {
					movieRefresh.onRefreshComplete();
					Toast.makeText(instance, R.string.refrash_all_finish, Toast.LENGTH_SHORT).show();
				}
			}
		});
		adapter = new MG_MediaAdapter(this, imageLoader);
		((GridView) listView).setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String urlString  = list_main.get(position).getVideo_url();
				AppLog.e(urlString);
				Video video  = vParser.parse(urlString);
				if (video != null) {
					AppLog.e(video.videoUri);
					Intent intent = new Intent();
					intent.putExtra("path", video.videoUri);
					intent.putExtra("title", list_main.get(position).getVideo_name());
					intent.setClass(MG_MOVIE.this, JieLiveVideoPlayer.class);
					startActivity(intent);
				}else {
					AppLog.e("null");
				}
			}
		});
		new InitData().execute();
		vParser = new VParser(this);
	}
	
	class InitData extends AsyncTask<Void, Void, Void> {
		InitData() {
		}
		List<Type_v_qq_com> lists = null;
		@Override
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				lists = dbUtils.getRoundMovies(index*100, (index+1)*100);
				for (Type_v_qq_com list : lists) {
					list_main.add(list);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			adapter.setListItem(list_main);
			movieRefresh.onRefreshComplete();
		}
	}
}
