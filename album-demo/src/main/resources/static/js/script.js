Vue.component('vue-gallery', {
    props: ['photos'],
    data: function () {
        return {
            activePhoto: null
        }
    },
    template: `
    <div class="vueGallery">
    <div class="activePhoto" :style="'background-image: url('+photos[activePhoto]+');'">
      <button type="button" aria-label="Previous Photo" class="previous" @click="previousPhoto()">
        <i class="fas fa-chevron-circle-left"></i>
      </button>
      <button type="button" aria-label="Next Photo" class="next" @click="nextPhoto()">
        <i class="fas fa-chevron-circle-right"></i>
      </button>
    </div>
    <div class="thumbnails">
      <div
           v-for="(photo, index) in photos"
           :src="photo"
           :key="index"
           @click="changePhoto(index)"
           :class="{'active': activePhoto == index}" :style="'background-image: url('+photo+')'">
      </div>
    </div>
  </div>`,
    mounted() {
        this.changePhoto(0)
        document.addEventListener("keydown", (event) => {
            if (event.which == 37)
                this.previousPhoto()
            if (event.which == 39)
                this.nextPhoto()
        })
    },
    methods: {
        changePhoto(index) {
            this.activePhoto = index
        },
        nextPhoto() {
            this.changePhoto(this.activePhoto + 1 < this.photos.length ? this.activePhoto + 1 : 0)
        },
        previousPhoto() {
            this.changePhoto(this.activePhoto - 1 >= 0 ? this.activePhoto - 1 : this.photos.length - 1)
        }
    }
});


Vue.component('v-tag', {
    props: ['tags'],
    template: `
    <div>
    <div class="tags-wrap"
        v-for="(value, key) in tags" :key="key">
        
    <div class="tags" transition="tags" :style="{backgroundColor: bgc[getBgcNo()]}" >
        <span class="content" @click="getCate(key)">{{cateMap[key]}}类</span>
    </div>
    <br>
    

    <div class="tags" transition="tags" 
        :style="{backgroundColor: bgc[getBgcNo()]}" 
        v-for="(item,index) in value" 
        :key="index" >
    <span class="content" @click="getPhotosByTag(key, item)">{{item}}</span>
    </div>
    </div>
     </div>`,
    data: function () {
        return {
            //"tags": {"cc": ["aa", "sb"], "dd": ["fff", "333"]},
            "bgc": ['#e961b4', '#ed664b', '#7b6ac7', '#56abd1', '#f7af4c', '#fe5467', '#52c7bd', '#a479b7', '#cb81ce', '#5eabc5'],
            "cateMap": {"expression": "表情", "scene": "场景"},


        }

    },
    methods: {
        getBgcNo: function () {
            return Math.ceil(Math.random() * 10) - 1
        },
        getPhotosByTag(cate, tag) {
            customEvent.$emit('getPhotosByCateAndLabel', cate, tag);
        },

        getCate(cate) {
            customEvent.$emit('getPhotosByCate', cate);

        }
    }
});






let customEvent = new Vue();//定义一个空的Vue实例

let tagIns = new Vue({
    el: "#my-custom-tags",
    data() {
        return {
            "tags": {}
        }

    },
    beforeMount() {
        this.getTags();

    },

    mounted() {
        customEvent.$on('refreshTag',success => {
            this.getTags();//箭头函数内部不会产生新的this，这边如果不用=>,this指代Event
        })
    },
    methods: {
        getBgcNo: function() {
            return Math.ceil(Math.random() * 10) - 1
        },

        getTags: async function() {
            let res = await axios.get("http://127.0.0.1:8080/album/v1/allCates")
            this.tags = res.data
        },


    }
});

let albumIns = new Vue({
    el: '#app',
    data() {
        return {
            photos: []
        }
    },
    created() {
        this.getData()
    },
    mounted() {
        customEvent.$on('uploadSuccess',success => {
            this.getData();//箭头函数内部不会产生新的this，这边如果不用=>,this指代Event
        })

        customEvent.$on("getPhotosByCateAndLabel", (cate, tag)=> {
            this.getPhotosByCateAndLabel(cate, tag)
        })

        customEvent.$on("getPhotosByCate", (cate)=> {
            this.getPhotosByCate(cate)
        })
    },
    methods: {
        getData: async function() {
            let res = await axios.get("http://127.0.0.1:8080/album/v1/list")
            this.photos = res.data
            console.log(res.data)
        },
        getPhotosByCateAndLabel: async function(cate, tag) {
            let url = `http://127.0.0.1:8080/album/v1/getPhotosByCateAndLabel?tag=${tag}&cate=${cate}`
            let resp = await axios.get(url)
            this.photos = resp.data
            console.log(resp.data)
        },
        getPhotosByCate: async function(cate) {
            let url = `http://127.0.0.1:8080/album/v1/getPhotosByCate?cate=${cate}`
            let resp = await axios.get(url)
            this.photos = resp.data
            console.log(resp.data)
        }
    }

});


let uplandIns = new Vue({
    el: '#upload',
    methods: {
        uploadSuccess: function(esponse, file, fileList) {
            customEvent.$emit('uploadSuccess', true);
            customEvent.$emit('refreshTag', true)
        }
    }
});



let vm = new Vue({
    el: '#appEvent',
    components: {
        'upload': uplandIns,
        'album':albumIns,
        "custom-tags": tagIns,
    }
});