package com.duapp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.dbay.apns4j.IApnsService;
import com.dbay.apns4j.demo.Apns4jDemo;
import com.dbay.apns4j.impl.ApnsServiceImpl;
import com.dbay.apns4j.model.ApnsConfig;
import com.dbay.apns4j.model.Feedback;
import com.dbay.apns4j.model.Payload;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApnsServiceServlet extends HttpServlet {

	final private static Logger log = Logger
			.getLogger(ApnsServiceServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static IApnsService apnsService;
	
	private static IApnsService getApnsService() {
		if (apnsService == null) {
			ApnsConfig config = new ApnsConfig();
			//InputStream is = Apns4jDemo.class.getClassLoader().getResourceAsStream(KeyKeeper.apnsFile());
			//config.setKeyStore(is);
			config.setKeyStore(KeyKeeper.apnsFile());
			config.setDevEnv(KeyKeeper.apnsDev());
			config.setPassword(KeyKeeper.apnsFileSecret());
			config.setPoolSize(5);
			config.setTimeout(30000);
			
			apnsService = ApnsServiceImpl.createInstance(config);
		}
		return apnsService;
	}
	
	public ApnsServiceServlet() {

	}

	public void init() throws ServletException {

	}

	public void destroy() {

	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		String conversationId = request.getParameter("c_id");
		String messageId = request.getParameter("m_id");
		//String myFeed = request.getParameter("my_feed");
		String message = request.getParameter("messages");
		String token = request.getParameter("token");
		String badge = request.getParameter("badge");
		String sound = request.getParameter("sound");

		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);
		params.put("c_id", conversationId);
		params.put("m_id", messageId);
		//params.put("my_feed", myFeed);
		params.put("token", token);
		params.put("badge", badge);
		params.put("sound", sound);

		// log user input.
		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("search parameters:").append(", token:").append(token);
			sb.append(", message:").append(message);
			//sb.append(", my_feed:").append(myFeed);
			sb.append(",c_id:").append(conversationId);
			sb.append(",m_id:").append(messageId);
			log.info(sb);
		}

		// set content-type header before accessing the Writer

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setBufferSize(8192);

		PrintWriter out = response.getWriter();
		Gson gson = new GsonBuilder().setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
		Object result = push_message_to_device(params);
		log.info ("json = "+gson.toJson(result));
		out.println(gson.toJson(result));

		out.flush();
		out.close();

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);

	}

	public String getServletInfo() {
		return "Zoie Service Loader Servlet.";
	}

	public Map<Object, Object> push_message_to_device(Map<String, String> params) {
		
		Map<Object, Object> return_result = new HashMap<Object, Object>();
		try{
			IApnsService service = getApnsService();
			
			// send notification
			String token = params.get("token");
			
			Payload payload = new Payload();
			payload.setAlert(params.get("message"));
			payload.setBadge(Integer.parseInt(params.get("badge")));
			payload.setSound(params.get("sound"));
			//payload.addParam("my_feed", params.get("my_feed"));
//			Map map = new HashMap();
//			map.put("c_id", params.get("c_id"));
//			map.put("m_id", params.get("m_id"));
//			payload.addParam("custome", map);
			payload.addParam("c_id", params.get("c_id"));
			payload.addParam("m_id", params.get("m_id"));
			service.sendNotification(token, payload);
			
			// get feedback
			List<Feedback> list = service.getFeedbacks();
			if (list != null && list.size() > 0) {
				return_result.put("error_message", list.get(0));
			}else{
				return_result.put("success_amount", "1");
			}
			
		}catch(Exception e){
			log.error(e);
			return_result.put("error_message", e.getMessage());
		}
		return return_result;
	}
	
	public static void main(String[] args){
		
	}

}
