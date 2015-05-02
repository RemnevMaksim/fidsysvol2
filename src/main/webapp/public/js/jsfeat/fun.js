
    var video = document.getElementById('webcam');
    var canvas = document.getElementById('canvas');
    var count_corners;
    var flag=false;
    try {
        var attempts = 0;
        var readyListener = function(event) {
            findVideoSize();
        };
        var findVideoSize = function() {
            if(video.videoWidth > 0 && video.videoHeight > 0) {
                video.removeEventListener('loadeddata', readyListener);
                onDimensionsReady(640, 480);
            } else {
                if(attempts < 10) {
                    attempts++;
                    setTimeout(findVideoSize, 200);
                } else {
                    onDimensionsReady(640, 480);
                }
            }
        };
        var onDimensionsReady = function(width, height) {
            demo_app(width, height);
            compatibility.requestAnimationFrame(tick);
        };

        video.addEventListener('loadeddata', readyListener);

        compatibility.getUserMedia({video: true}, function(stream) {
            try {
                video.src = compatibility.URL.createObjectURL(stream);
            } catch (error) {
                video.src = stream;
            }
            setTimeout(function() {
                video.play();
            }, 500);
        }, function (error) {
            $('#canvas').hide();
            $('#log').hide();
            $('#no_rtc').html('<h4>WebRTC not available.</h4>');
            $('#no_rtc').show();
        });
    } catch (error) {
        $('#canvas').hide();
        $('#log').hide();
        $('#no_rtc').html('<h4>Something goes wrong...</h4>');
        $('#no_rtc').show();
    }

    var stat = new profiler();

    var gui,options,ctx,canvasWidth,canvasHeight;
    var img_u8, corners, threshold;

    var demo_opt = function(){
        this.threshold = 10;
    }

    function demo_app(videoWidth, videoHeight) {
        canvasWidth  = canvas.width;
        canvasHeight = canvas.height;
        ctx = canvas.getContext('2d');

        ctx.fillStyle = "rgb(0,255,0)";
        ctx.strokeStyle = "rgb(0,255,0)";

        img_u8 = new jsfeat.matrix_t(640, 480, jsfeat.U8_t | jsfeat.C1_t);

        corners = [];
        var i = 640*480;
        while(--i >= 0) {
            corners[i] = new jsfeat.keypoint_t(0,0,0,0);
        }

        threshold = 20;

        jsfeat.fast_corners.set_threshold(threshold);

        options = new demo_opt();
        gui = new dat.GUI();

        gui.add(options, 'threshold', 5, 100).step(4);

        stat.add("grayscale");
        stat.add("fast corners");
    }

    function tick() {
        compatibility.requestAnimationFrame(tick);
        stat.new_frame();
        if (video.readyState === video.HAVE_ENOUGH_DATA && !flag) {
            ctx.drawImage(video, 0, 0, 640, 480);
            var imageData = ctx.getImageData(0, 0, 640, 480);

            stat.start("grayscale");
            jsfeat.imgproc.grayscale(imageData.data, 640, 480, img_u8);
            stat.stop("grayscale");

            if(threshold != options.threshold) {
                threshold = options.threshold|0;
                jsfeat.fast_corners.set_threshold(threshold);
            }

            stat.start("fast corners");
            var count = jsfeat.fast_corners.detect(img_u8, corners, 5);
            count_corners=count;
            stat.stop("fast corners");

            // render result back to canvas
            var data_u32 = new Uint32Array(imageData.data.buffer);
            render_corners(corners, count, data_u32, 640);

            ctx.putImageData(imageData, 0, 0);

            $('#log').html(stat.log());
        }
    }

    function render_corners(corners, count, img, step) {
        var pix = (0xff << 24) | (0x00 << 16) | (0xff << 8) | 0x00;
        for(var i=0; i < count; ++i)
        {
            var x = corners[i].x;
            var y = corners[i].y;
            var off = (x + y * step);
            img[off] = pix;
            img[off-1] = pix;
            img[off+1] = pix;
            img[off-step] = pix;
            img[off+step] = pix;
            text.value = 'x: '+x+ ' y: '+y+ ' count: '+count;
        }
    }
var text=document.getElementById("text_area");
function take_photo(){
    flag=true;
}
function continue_video(){
    flag=false;
}
    function fillCell(table, tr) {
        var tr11 = document.getElementById(tr); //берем первую строку
        var table = document.getElementById(table);
        for(var i=0;i<count_corners;++i){
            var x = corners[i].x;
            var y = corners[i].y;
            var tr31 = document.createElement('tr'); //создаем еще строку
            var td31 = document.createElement('td'); td31.innerHTML = x; //создаем столбец
            var td32 = document.createElement('td'); td32.innerHTML = y; //создаем еще столбец
            // tr11.appendChild(td31); так можно добавить в первую строку столбец
            tr31.appendChild(td31); //кладем в новосозданную строку первый новосозданный столбец
            tr31.appendChild(td32); //кладем в новосозданную строку второй новосозданный столбец
            table.appendChild(tr31); //кладем в таблицу новосозданную строку (последней)
        }

    }
    function Point(x,y){
        this.x=x;
        this.y=y;
    }
    function send(){
        var points=[];
        for(var i=0;i<count_corners;++i){
            points[i]=new Point(corners[i].x,corners[i].y);
        }
        var pointsJson = JSON.stringify(points);

        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open( "POST", "/try/steptwo", false );
        xmlHttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xmlHttp.setRequestHeader("Content-length", corners.length);
        xmlHttp.send(pointsJson);
        return xmlHttp.responseText;
    }
