package com.duapp.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PusherServiceServlet extends HttpServlet {

	final private static Logger log = Logger
			.getLogger(PusherServiceServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PusherServiceServlet() {

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
		String user_id = request.getParameter("user_id");
		String channel_id = request.getParameter("channel_id");
		String message = request.getParameter("messages");

		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);
		params.put("channel_id", channel_id);
		params.put("user_id", user_id);

		// log user input.
		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("search parameters:").append(",user_id:").append(user_id)
					.append(",channel_id:").append(channel_id);
			sb.append(",message:").append(message);
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
		/*
		 * @brief 推送单播消息(消息类型为透传，由开发方应用自己来解析消息内容) message_type = 0 (默认为0)
		 */

		// 1. 设置developer平台的ApiKey/SecretKey
		String apiKey = KeyKeeper.api_key();
		String secretKey = KeyKeeper.secret_key();
		ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);

		// 2. 创建BaiduChannelClient对象实例
		BaiduChannelClient channelClient = new BaiduChannelClient(pair);

		// 3. 若要了解交互细节，请注册YunLogHandler类
		// channelClient.setChannelLogHandler(new YunLogHandler() {
		// @Override
		// public void onHandle(YunLogEvent event) {
		// System.out.println(event.getMessage());
		// }
		// });

		Map<Object, Object> return_result = new HashMap<Object, Object>();

		String channel_id = params.get("channel_id");
		String user_id = params.get("user_id");

		try {

			// 4. 创建请求类对象
			// 手机端的ChannelId， 手机端的UserId， 先用1111111111111代替，用户需替换为自己的
			PushUnicastMessageRequest request = new PushUnicastMessageRequest();
			request.setDeviceType(3); // device_type => 1: web 2: pc 3:android
										// 4:ios 5:wp

			request.setChannelId(Long.valueOf(channel_id));
			request.setUserId(user_id);

			request.setMessage(params.get("message"));

			// 5. 调用pushMessage接口
			PushUnicastMessageResponse response = channelClient
					.pushUnicastMessage(request);

			// 6. 认证推送成功
			System.out.println("push amount : " + response.getSuccessAmount());
			return_result.put("success_amount", response.getSuccessAmount());

		} catch (ChannelClientException e) {
			// 处理客户端错误异常
			log.error(e);
		} catch (ChannelServerException e) {
			// 处理服务端错误异常
			log.error(String.format(
					"request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));

			return_result.put("request_id", e.getRequestId());
			return_result.put("error_code", e.getErrorCode());
			return_result.put("error_message", e.getErrorMsg());

		}

		return return_result;

	}

}
