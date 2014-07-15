package de.kisi.android.api;

import org.jivesoftware.smack.XMPPException;
import android.util.Log;
import com.google.gson.Gson;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.auth.QBAuth;
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
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;

import de.kisi.android.Config;
import de.kisi.android.KisiApplication;
import de.kisi.android.messages.Message;
import de.kisi.android.messages.PlayServicesHelper;
import de.kisi.android.model.User;

/**
 * This is a wrapper to communicate with Quickblox servers. 
 *
 */
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

	/**
	 * Getter for currentQbUser.
	 * @return
	 */
	public QBUser getCurrentQbUser() {
		return qBUser;
	}

	/**
	 * This function is used to login to QuickBlox servers. When the login is completed, 
	 * it calls the onComplete method in callback. It gets the current user from KisiAPI.
	 * If the user is not logged in to KISI servers, it creates a new user in quickblox 
	 * servers by using the deviceID.
	 * @param callback
	 */
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
			//create temporary user based on unique device id
			String uid = new PlayServicesHelper().getDeviceID();
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

	/**
	 * Sends a push notification message to the user.
	 * @param message
	 * @param qbUserId
	 */
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
	
	/**
	 * Wraps the getUserByLogin method from QBUsers. It gets a user from his username. 
	 * @param login
	 * @param callback
	 */
	public void getUserByLogin(String login, QBCallback callback){
		QBUsers.getUserByLogin(login, callback);
	}

	/**
	 * Sets and initializes the video chat listener in QBVideoChatController.
	 * @param qbVideoChatListener
	 */
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
	
	/**
	 * Initializes the video chat. If the user is not logged in, It tries to login.
	 * @param qbVideoChatListener
	 */
	public void initVideoChat(final OnQBVideoChatListener qbVideoChatListener){
		if(this.getCurrentQbUser() == null){
			this.login(new QBCallback() {
				@Override public void onComplete(Result arg0, Object arg1) {}
				
				@Override
				public void onComplete(Result result) {
					if(result.isSuccess()){
						QuickBloxApi.getInstance().initVideoChat(qbVideoChatListener);;
					}
				}
			});
		}else{
			if(!QBChatService.getInstance().isLoggedIn()){		
				QBChatService.getInstance().loginWithUser(this.getCurrentQbUser(), new SessionCallback() {
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
	}

	/**
	 * Accepts a video call request.
	 * @param videoChatConfig
	 */
	public void acceptVideoChat(VideoChatConfig videoChatConfig){
		QBVideoChatController.getInstance().acceptCallByFriend(videoChatConfig, null);
	}

	/**
	 * Stops the video chat service.
	 */
	public void stopVideoChat(){
		QBVideoChatController.getInstance().stopCalling();
		QBVideoChatController.getInstance().clearVideoChatsList();
		QBChatService.getInstance().logout();
	}

}
