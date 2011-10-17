<!DOCTYPE html>
<html>
	<head>
        <!--<base href="${pagecontext.request.contextpath}" />-->
        <title>Fabflix - <%= (String) session.getAttribute("title") %></title>
		<!--<link rel="stylesheet" href="css/normalize.css" type="text/css" />-->
		<!--<link rel="stylesheet" href="css/style.css" type="text/css" />-->
	</head>

	<body>
	
	<style>
		<%@ include file="css/style.css" %>
	</style>
	
	<%@ include file="menu.jsp" %>
	
	<div class="content">