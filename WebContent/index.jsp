<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.sun.jersey.api.client.*"%>
<%@page import="com.sun.jersey.api.client.Client"%>
<%@page import="com.sun.jersey.api.client.WebResource"%>
<%@page import="com.sun.jersey.api.client.config.ClientConfig"%>
<%@page import="com.sun.jersey.api.client.config.DefaultClientConfig"%>
<%@page import="javax.ws.rs.core.UriBuilder"%>
<%@page import="javax.ws.rs.core.MediaType"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<% String name=request.getParameter("name"); 
ClientConfig cfg=new DefaultClientConfig(); 
Client client = Client.create(cfg); 
WebResource service = client.resource(UriBuilder.fromUri("http://10.163.2.104:8085/newweb/").build()); 
String xml = service.path("helloworld").path(name).accept(MediaType.TEXT_PLAIN).get(String.class); 
out.println("XML==>"+xml);
%>
</body>
</html>