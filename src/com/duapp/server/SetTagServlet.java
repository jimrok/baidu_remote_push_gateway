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
import com.baidu.yun.channel.model.SetTagRequest;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SetTagServlet extends HttpServlet {

	final private static Logger log = Logger
			.getLogger(BindListQueryServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetTagServlet() {

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
		String tag = request.getParameter("tag");

		// log user input.
		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("user_id:");
			sb.append(user_id);
			log.info(sb);
		}

		// set content-type header before accessing the Writer

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setBufferSize(128);

		PrintWriter out = response.getWriter();
		// JSONObject json_object = new JSONObject();
		Gson gson = new GsonBuilder().setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
		
		out.println(gson.toJson(setUserTag(user_id,tag)));
		
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

	private Map<Object, Object>  setUserTag(String user_id, String tag) {
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
		// // TODO Auto-generated method stub
		// System.out.println(event.getMessage());
		// }
		// });
		
		Map<Object, Object> return_result = new HashMap<Object, Object>();
		try {
			// 4. 创建请求类对象
			// 手机端的UserId， 先用1111111111111代替，用户需替换为自己的
			SetTagRequest request = new SetTagRequest();
			request.setUserId(user_id);
			request.setTag(tag);

			// 5. 调用setTag接口
			channelClient.setTag(request);
			return_result.put("status", 200);

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