const debug = true;

const globals = {
    appPath: '',
    lamber93: {
        e: 0.0824832568,
        n: 0.760405966,
        c: 11603796.9767,//11603796.9767,
        lc: 0.047923443,
        xs: 600000,//600000,
        ys: 5657616.674//5657616.674,
    }
};

const utils = {
    /**
     * Return the root path of the app (for example '/test')
     * @returns {string} the computed rootpath
     */
    getRootPath: function () {
        let rootPath = window.location.pathname;
        if (rootPath.endsWith('/'))
            rootPath = rootPath.substr(0, rootPath.length - 1);
        return rootPath;
    },
    ellipse: function (context, cx, cy, rx, ry) {
        context.save(); // save state
        context.beginPath();

        context.translate(cx - rx, cy - ry);
        context.scale(rx, ry);
        context.arc(1, 1, 1, 0, 2 * Math.PI, false);

        context.restore(); // restore to original state
        context.fill();
    },
    radians: function (degrees) {
        return degrees * Math.PI / 180;
    }
};

const lamber93 = {
    e: 0.0824832568,
    n: 0.760405966,
    c: 11603796.9767,//11603796.9767,
    lc: 0.047923443,
    xs: 600000,//600000,
    ys: 5657616.674,//5657616.674,
    convert: function (lat, long) {
        const p = utils.radians(lat);
        const l = utils.radians(long);

        const lati = Math.log(Math.tan(Math.PI / 4 + p / 2) * Math.pow((1 - this.e * Math.sin(p)) / (1 + this.e * Math.sin(p)), this.e / 2));

        return [
            this.xs + this.c * Math.exp(-this.n * lati) * Math.sin(this.n * (l - this.lc)),
            this.ys - this.c * Math.exp(-this.n * lati) * Math.cos(this.n * (l - this.lc))
        ];
    }
};

const map = function () {
    const c0 = lamber93.convert(50, -4);
    const c1 = lamber93.convert(42, 8);
    const fx = (426 - 51) / (c1[0] - c0[0]);
    const fy = (446 - 66) / (c1[1] - c0[1]);
    const x0 = 51 - (c0[0] * fx);
    const y0 = 66 - (c0[1] * fy);
    this.convert = function (lat, long) {
        const v = lamber93.convert(lat, long);
        return [
            v[0] * fx + x0,
            v[1] * fy + y0
        ];
    };
    return this;
}();

/**
 * ajax global to handle all ajax calls
 * @author mortrevere, 2018
 */
const ajax = {
    /**
     *
     * @param {string} method
     * @param {string} URI
     * @param {Object | function} [data] - request data or callback
     * @param {function} [callback] - callback or onerror if data is callback
     * @param {function} [onerror]
     */
    call: function (method, URI, data, callback, onerror) {
        const o = {
            method: method,
            url: globals.appPath + '/api' + URI
        };

        if (typeof data === 'function') {
            onerror = callback;
            callback = data;
        } else if (typeof data === 'object') {
            o.data = data;
        }

        function cbwrap(data) {
            if (debug)
                console.log('Received response from ' + o.method + ' request to ' + o.url + ' : ', data, 'sent data :', o.data);

            data = data.value ? data.value : data;
            if (callback)
                callback(data);
        }

        function failwrap(data) {
            if (data.status === 401)
                window.location.href = globals.appPath + '/login?redirect=' + encodeURI(window.location.href);

            console.error('Error in ' + o.method + ' request to ' + o.url + ' (status : ' + data.status + ') : ', data);

            if (onerror)
                onerror(data);
        }

        $.ajax(o).done(cbwrap).fail(failwrap);
    }
};

$(document).ready(function () {
    globals.appPath = utils.getRootPath();
    $.ajaxSetup({
        cache: true
    });

    $('form').submit(function () {
        return false;
    });

});

const app = new Vue({
    el: '#app',
    data: {
        cities: [
            {
                hints: [],
                show: false,
                selected: -1,
            },
            {
                hints: [],
                show: false,
                selected: -1
            }
        ],
        dist: 0
    },
    methods: {
        'input': function (i) {
            const self = this;
            const city = self.cities[i];

            if (city.timeout)
                clearTimeout(city.timeout);

            self.clear(i);

            city.timeout = setTimeout(function () {
                city.show = true;
                self.getData(i);
            }, 500);
        },
        'getData': function (i) {
            const self = this;
            const city = self.cities[i];
            if (city.value && city.value.length > 2) {
                city.hints = [];
                const p = {
                    query: city.value
                };
                ajax.call('GET', '/search', p, function (value) {
                    value.forEach(function (hint) {
                        hint.text = hint.name + ' <small>' + hint.postalCodes + '</small>';
                        city.hints.push(hint);
                    });
                });
            }
        },
        'keydown': function (i, e) {
            const self = this;
            const city = self.cities[i];

            switch (e.keyCode) {
                case 40://down
                    if (city.selected < city.hints.length)
                        city.selected++;
                    else
                        city.selected = 0;
                    break;
                case 38: //up
                    if (city.selected > 0)
                        city.selected--;
                    else
                        city.selected = city.hints.length;
                    break;
                case 13: //enter
                    e.preventDefault();
                    self.select(i);
                    break;
            }
        },
        'match': function (src, m) {
            const mi = src.toLowerCase().indexOf(m);
            if (mi < 0) {
                return src;
            } else {
                let out = '';
                out += src.substr(0, mi);
                src = src.substr(mi);

                out += '<b>' + src.substr(0, m.length) + '</b>';
                src = src.substr(m.length);

                out += src;
                return out;
            }
        },
        'blur': function (i) {
            const self = this;
            const city = self.cities[i];

            setTimeout(function () {
                city.show = false;
            }, 100);
        },
        'clear': function (i) {
            const self = this;
            const city = self.cities[i];
            city.hints = [];
            city.selected = -1;
            city.show = false;
            city.current = undefined;
            self.refreshMap();
        },
        'select': function (i, j) {
            const self = this;
            const city = self.cities[i];

            if (j !== undefined)
                city.selected = j;

            if (city.selected >= 0) {
                city.value = city.hints[city.selected].name;
                const tmp = city.hints[city.selected];
                self.clear(i);
                city.current = tmp;
                self.refreshMap();
            }
        },
        'refreshMap': function () {
            this.ctx.drawImage(document.getElementById('map-image'), 10, 10, 980, 950, 0, 0, 500, 500);
            this.ctx.fillStyle = '#D32F2F';
            this.ctx.strokeStyle = '#D32F2F';
            this.ctx.lineWidth = 1;
            let c0;
            let c1;
            if (this.cities[0].current) {
                c0 = map.convert(this.cities[0].current.geoLong, this.cities[0].current.geoLat);
                utils.ellipse(this.ctx, c0[0] - 1, c0[1] - 1, 3, 3);
            }
            if (this.cities[1].current) {
                c1 = map.convert(this.cities[1].current.geoLong, this.cities[1].current.geoLat);
                utils.ellipse(this.ctx, c1[0] - 1, c1[1] - 1, 3, 3);
            }
            if (c0 && c1) {
                this.ctx.beginPath();
                this.ctx.moveTo(c0[0], c0[1]);
                this.ctx.lineTo(c1[0], c1[1]);
                this.ctx.closePath();
                this.ctx.stroke();

                const cl0 = lamber93.convert(this.cities[0].current.geoLong, this.cities[0].current.geoLat);
                const cl1 = lamber93.convert(this.cities[1].current.geoLong, this.cities[1].current.geoLat);

                this.dist = ((Math.sqrt(Math.pow(cl0[0] - cl1[0], 2) + Math.pow(cl0[1] - cl1[1], 2)))/1000).toFixed(0);
            }else{
                this.dist = 0;
            }
        }
    },
    mounted: function () {
        const canvas = document.getElementById('map');
        this.ctx = canvas.getContext('2d');
        this.refreshMap();
    }
});
