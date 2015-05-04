<#import "/layout/mainlayout.ftl" as layout />
<@layout.layout title="Get photo">
<style>

    canvas{
        width: 100%;
        height: 100%;
        margin: 0px;
    }
</style>
    <!--
    <div class="col-md-12   ">
    <div class="page-header">
        <h2>FIDsys</h2>
    </div>
<div class="panel-body" id="container"  >
    <div class="select">
        <label for="audioSource">Audio source: </label><select id="audioSource"></select>
    </div>
    <div class="select">
        <label for="videoSource">Video source: </label><select id="videoSource"></select>
    </div>
        <video id="video" autoplay="autoplay" ></video>
  <script src="/public/js/main.js"></script>
    <p>This demo requires Chrome 30 or later.</p>
</div>
    </div>-->
<h2>FidSYS demo.</h2>
<br>
<h3>Step 1</h3>
<div class="panel-body" align="center" id="container"  >
    <!--<div class="select">
        <label for="videoSource">Video source: </label><select id="videoSource"></select>
    </div>-->
    <video id="webcam" width="640" height="480"  style="display:none;"></video>
    <div>
        <canvas id="canvas" width="640" height="480"></canvas>
        <div id="no_rtc" class="alert alert-error" style="display:none;"></div>
        <div id="log" class="alert alert-info"></div>
    </div>
    <div class="form-inline" role="form">
        <input type="text"  id="text_area"/>
        <input type="button" class="btn btn-success" value="take photo" onclick="take_photo()"/>
        <input type="button" class="btn btn-primary"value="continue" onclick="continue_video()"/>
    </div>
    <div  id="myDiv"><h2>Test text</h2></div>
    <div class="form-inline" role="form">
    <input type="button" class="btn btn-primary"value="continue" onclick="send()"/>
    <input type="button" id="nextstep" class="btn btn-primary"value="next step" style="display: none"/>
        </div>
    </div>

</@layout.layout>