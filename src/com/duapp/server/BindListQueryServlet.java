package com.duapp.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.BindInfo;
import com.baidu.yun.channel.model.QueryBindListRequest;
import com.baidu.yun.channel.model.QueryBindListResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BindListQueryServlet extends HttpServlet {

	final private static Logger log = Logger
			.getLogger(BindListQueryServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BindListQueryServlet() {

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
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("channel_id", getUserChannelId(user_id));
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

	private String getUserChannelId(String user_id) {
		// 1. ����developerƽ̨��ApiKey/SecretKey
		String apiKey = KeyKeeper.api_key();
		String secretKey = KeyKeeper.secret_key();
		ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);

		// 2. ����BaiduChannelClient����ʵ��
		BaiduChannelClient channelClient = new BaiduChannelClient(pair);

		// 3. ��Ҫ�˽⽻��ϸ�ڣ���ע��YunLogHandler��
		// channelClient.setChannelLogHandler(new YunLogHandler() {
		// @Override
		// public void onHandle(YunLogEvent event) {
		// // TODO Auto-generated method stub
		// System.out.println(event.getMessage());
		// }
		// });

		try {
			// 4. �������������
			// �ֻ��˵�UserId�� ����1111111111111���棬�û����滻Ϊ�Լ���
			QueryBindListRequest request = new QueryBindListRequest();
			request.setUserId(user_id);

			// 5. ����queryBindList�ӿ�
			QueryBindListResponse response = channelClient
					.queryBindList(request);

			// 6. �Է��صĽ��������в���
			List<BindInfo> bindInfos = response.getBinds();

			String channel_id = null;
			for (BindInfo bindInfo : bindInfos) {
				long channelId = bindInfo.getChannelId();
				channel_id = String.valueOf(channelId);
				break;
			}

			return channel_id;
		} catch (ChannelClientException e) {
			// ����ͻ��˴����쳣
			log.error("execute error in user_id:" + user_id, e);
		} catch (ChannelServerException e) {
			// �������˴����쳣
			log.error(String.format(
					"request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
			
		}

		return null;
	}

	public static void main(String[] args) {
		BindListQueryServlet s = new BindListQueryServlet();
		String id = s.getUserChannelId("800121404573404046");
		System.out.println("cid:" + id);
	}

}
