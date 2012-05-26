package com.mi.miboxes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadFile extends AsyncTask<String, Integer, String> {

	private Context ctx;
	private final ProgressDialog dlDialog;
	private String fileName;
	private String downloadedFile;

	public DownloadFile(Context context, String downloadFileName) {

		ctx = context.getApplicationContext();
		dlDialog = new ProgressDialog(context);
		dlDialog.setMessage("Downloading...");
		dlDialog.setIndeterminate(false);
		dlDialog.setMax(100);
		dlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		fileName = downloadFileName;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dlDialog.show();
		Log.d("DownloadFile onPreExecute", "PreExecute Start");
	}

	@Override
	protected String doInBackground(String... sUrl) {

		return downloadedFile = downloadFileFromDropBox(sUrl);

	}
	
	protected String downloadFileFromDropBox(String... path) {
		String resultPath;

		try {
			URL url = new URL(path[0]);
			Log.d("DownloadFile doInBackground", "url=" + url);
			URLConnection conn = url.openConnection();
			conn.connect();

			int fileLen = conn.getContentLength();
			// String cachePath = ctx.getCacheDir().getAbsolutePath() + "/" +
			// fileName;
			Log.d("DownloadFile doInBackground", "File Length=" + fileLen);

			String desPath = "/mnt/sdcard/MCSIS/";
			File MCSIS_DIR = new File(desPath);
			if (!MCSIS_DIR.exists())
				MCSIS_DIR.mkdir();
			// Download the file
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(desPath + fileName);

			resultPath = desPath + fileName;

			Log.d("DownloadFile doInBackground", "downloadedFile="
					+ downloadedFile);

			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				// Log.d("DownloadFile doInBackground", "count=" + count);

				total += count;

				// Log.d("DownloadFile doInBackground", "total=" + total);

				// Publishing the progress...
				// Log.d("DownloadFile doInBackground",
				// "before publishProgress=" + total);

				publishProgress((int) (total * 100 / fileLen));
				// Log.d("DownloadFile doInBackground", "after publishProgress="
				// + total);

				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();

		} catch (Exception e) {
			Log.e("DownloadFile doInBackground", e.toString());
			return null;
		}
		return resultPath;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		dlDialog.setProgress(progress[0]);
		// Log.d("DownloadFile onProgressUpdate", "progress=" + progress);

	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		dlDialog.dismiss();
		Toast.makeText(ctx, "File Download Finished!", Toast.LENGTH_SHORT)
				.show();
		openFile(result);
	}

	protected void openFile(String f) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(f);
		// intent.setDataAndType(Uri.fromFile(file), "video/*");
		intent.setData(Uri.fromFile(file));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);

		// Log.d("DownloadFile openFile", "file=" + file.getName());

		/*
		 * try { ctx.startActivity(intent); } catch (Exception e) {
		 * Log.e("DownloadFile openFile", e.getLocalizedMessage()); }
		 */
	}

}
