/**
 * 商品表单：选择本地图片后在页面展示缩略图预览（不上传前即可见）。
 */
(function () {
    var input = document.querySelector('input[name="images"][type="file"][multiple]');
    var box = document.getElementById('productImagesPreviewBox');
    var grid = document.getElementById('productImagesPreview');
    if (!input || !box || !grid) {
        return;
    }

    var objectUrls = [];

    function revokeAll() {
        objectUrls.forEach(function (u) {
            try {
                URL.revokeObjectURL(u);
            } catch (e) {}
        });
        objectUrls = [];
        grid.innerHTML = '';
    }

    input.addEventListener('change', function () {
        revokeAll();
        var files = this.files;
        if (!files || files.length === 0) {
            box.hidden = true;
            return;
        }

        var shown = 0;
        for (var i = 0; i < files.length; i++) {
            var f = files[i];
            if (!f.type || f.type.indexOf('image/') !== 0) {
                continue;
            }
            var url = URL.createObjectURL(f);
            objectUrls.push(url);

            var item = document.createElement('div');
            item.className = 'product-form-preview-item';

            var img = document.createElement('img');
            img.src = url;
            img.alt = f.name || '';

            var cap = document.createElement('span');
            cap.className = 'product-form-preview-name';
            cap.textContent = f.name || '图片';
            cap.title = f.name || '';

            item.appendChild(img);
            item.appendChild(cap);
            grid.appendChild(item);
            shown++;
        }

        box.hidden = shown === 0;
    });
})();
