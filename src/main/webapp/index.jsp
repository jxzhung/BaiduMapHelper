<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<title>百度地图数据导出</title>
<style>
    body{
        padding: 10px;
    }
</style>
<body>
百度地图数据导出工具
<h2>请填写种子URL</h2>
<form action="baidu/map-export" method="post">
    <input type="text" value="" name="rawUrl">
    <button>下载</button>
</form>
注意：点击下载后稍等片刻等待后台处理完毕自动弹出下载框<br/><br/>
如何找到种子URL？ <br/>
进入百度地图 <a href="http://map.baidu.com/" title="点我跳转" target="_blank">点我进入百度地图</a>
<br/>
<img src="images/steps.png">

</body>
</html>
