package de.kisi.android.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.SessionCallback;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushType;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.definition.VideoChatConstants;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;

import de.kisi.android.Config;
import de.kisi.android.KisiApplication;
import de.kisi.android.messages.Message;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;

public class QuickBloxApi {

	// -------------------- Singleton Stuff: --------------------
	private static QuickBloxApi instance;

	public static synchronized QuickBloxApi getInstance() {
		if(instance == null)
			instance = new QuickBloxApi();
		return instance;
	}
	private QuickBloxApi() {
		SmackAndroid.init(KisiApplication.getInstance());
		QBSettings.getInstance().fastConfigInit(Config.APP_ID, Config.AUTH_KEY, Config.AUTH_SECRET);
		Log.d("QuickBloxAPI", "Configured QB");
	}


	// -------------------- QuickBlox Stuff: --------------------
	private QBUser qBUser = null;

	public void setCurrentQbUser(int currentQbUserId, String login, String password) {
		this.qBUser = new QBUser(currentQbUserId);
		this.qBUser.setLogin(login);
		this.qBUser.setPassword(password);
	}

	public QBUser getCurrentQbUser() {
		return qBUser;
	}


	public void login(final QBCallback callback){

		Log.d("QuickBloxAPI","Logging in...");

		final String login;
		final String password;

		User currentUser = KisiAPI.getInstance().getUser();
		if (currentUser != null){
			login = "kisi-" + currentUser.getId();
			password = "12345678"; // TODO Change this later..
		}
		else{ //create temporary QB user
			Log.d("QuickBloxAPI", "logging in with temporary user...");
			String uid = "12345"; //create temporary user based on unique device id
			login = "tempUser"+uid;
			password = Config.USER_PASSWORD;
		}
		final QBUser user = new QBUser(login, password);



		QBAuth.createSession(new QBCallback() {
			@Override
			public void onComplete(Result result, Object o) {}
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()){

					//test if user exists on server
					QBUsers.getUserByLogin(login, new QBCallback() {
						@Override
						public void onComplete(Result result, Object o) {}
						@Override
						public void onComplete(Result result) {
							if (result.isSuccess()){
								QBUsers.signIn(user, new QBCallback() {
									@Override
									public void onComplete(Result result, Object o) {}
									@Override
									public void onComplete(Result result) {
										if (result.isSuccess()){
											setCurrentQbUser(((QBUserResult) result).getUser().getId(), login , password);
											Log.d("QuickBloxAPI","Logged in");
											callback.onComplete(result);
										}else{
											Log.e("QuickBloxAPI", "sign in unsuccessful: "+result.getErrors());
										}
									}
								});
							}else{
								Log.e("QuickBloxAPI", "Couldn't find user on server: "+result.getErrors());

								QBUsers.signUpSignInTask(user, new QBCallback() {
									@Override
									public void onComplete(Result result, Object o) {}
									@Override
									public void onComplete(Result result) {
										if (result.isSuccess()){
											setCurrentQbUser(((QBUserResult) result).getUser().getId(), login , password);
											Log.d("QuickBloxAPI","Logged in");
											callback.onComplete(result);
										}else{
											Log.e("QuickBloxAPI", "sign up sign in unsuccessful: "+result.getErrors());
										}
									}
								});
							}
						}
					});
				}else{
					Log.e("QuickBloxAPI", "Create session unsuccessful: "+result.getErrors());
				}
			}
		});
	}

	public void sendMessage(Message message, int qbUserId) {
		// Send Push: create QuickBlox Push Notification Event
		QBEvent qbEvent = new QBEvent();
		qbEvent.setNotificationType(QBNotificationType.PUSH);
		qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);
		String messageJson = new Gson().toJson(message);
		Log.d("QuickBloxAPI", "Send message: "+messageJson);
		qbEvent.setMessage(messageJson);
		StringifyArrayList<Integer> recipientsIds = new StringifyArrayList<Integer>();
		recipientsIds.add(qbUserId);
		qbEvent.setUserIds(recipientsIds);
		qbEvent.setUserId(QuickBloxApi.getInstance().getCurrentQbUser().getId());
		qbEvent.setPushType(QBPushType.GCM);
		QBMessages.createEvent(qbEvent, new QBCallback() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()){
					Log.d("QuickBloxAPI", "Sent message successfully.");
				}else{
					Log.d("QuickBloxAPI", "Send message error: "+result.getErrors());
				}
			}
			@Override
			public void onComplete(Result result, Object o) {
			}
		});
	}

	public void getUserByLogin(String login, QBCallback callback){
		QBUsers.getUserByLogin(login, callback);
	}

	private void setVideoChatListener(OnQBVideoChatListener qbVideoChatListener){
		try {
			QBVideoChatController.getInstance().setQBVideoChatListener(QuickBloxApi.getInstance().getCurrentQbUser(), qbVideoChatListener);
			QBVideoChatController.getInstance().initQBVideoChatMessageListener();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	public void initVideoChat(final OnQBVideoChatListener qbVideoChatListener){
		if(!QBChatService.getInstance().isLoggedIn()){		
			QBChatService.getInstance().loginWithUser(QuickBloxApi.getInstance().getCurrentQbUser(), new SessionCallback() {
				@Override
				public void onLoginSuccess() {
					QuickBloxApi.getInstance().setVideoChatListener(qbVideoChatListener);
				}

				@Override
				public void onLoginError(String arg0) {
					// TODO Error when login
					Log.e("QuickBloxLogin", "Error When Login");

				}
			});

		}else{
			QuickBloxApi.getInstance().setVideoChatListener(qbVideoChatListener);
		}
	}

	public void acceptVideoChat(VideoChatConfig videoChatConfig){
		QBVideoChatController.getInstance().acceptCallByFriend(videoChatConfig, null);
	}

	public void stopVideoChat(){
		QBVideoChatController.getInstance().stopCalling();
		QBVideoChatController.getInstance().clearVideoChatsList();
		QBChatService.getInstance().logout();
	}

}
