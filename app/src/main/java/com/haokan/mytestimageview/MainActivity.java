package com.haokan.mytestimageview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class MainActivity extends AppCompatActivity {

    public static final String[] picData = new String[]{
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fcdn.duitang.com%2Fuploads%2Fitem%2F201303%2F29%2F20130329205806_kTTnv.thumb.700_0.jpeg&refer=http%3A%2F%2Fcdn.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631280979&t=ac3ffc327dcc2f074c7bff3198212cf2",
            "https://ss0.baidu.com/94o3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/b64543a98226cffc86abe943bc014a90f703eaba.jpg",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0319%252F30902a39j00qq7hp000sqc000u001hcm.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808541&t=3e477e8d3affd57b777802605e471f1e"
    };

    public static final String[] picDataMore = new String[]{
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fcdn.duitang.com%2Fuploads%2Fitem%2F201303%2F29%2F20130329205806_kTTnv.thumb.700_0.jpeg&refer=http%3A%2F%2Fcdn.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631280979&t=ac3ffc327dcc2f074c7bff3198212cf2",
            "https://t8.baidu.com/it/u=1484500186,1503043093&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1597826769&t=5aad7287dfe219994a7a59f78aca0006",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201605%2F10%2F20160510001106_2YjCN.thumb.700_0.jpeg&refer=http%3A%2F%2Fb-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281036&t=494931d2f88600781ccc98941c1c171e",
            "https://ss0.baidu.com/94o3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/b64543a98226cffc86abe943bc014a90f703eaba.jpg",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0319%252F30902a39j00qq7hp000sqc000u001hcm.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808541&t=3e477e8d3affd57b777802605e471f1e",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0812%252Ff2ed8a22j00qxp4oy004yc000dc01u1c.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631337174&t=60592ca90695c685514a5c60272aec57",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F48124ed00ae163228d4e65acf0d54c5cc5a2f31a2e142-PaXGuD_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281100&t=b2226ca4bcde9a4fffc5bc4e2300d1ac",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fimages%2F20190917%2F2603e4d3e9f54ec08cd22d9d9cb6b539.JPG&refer=http%3A%2F%2F5b0988e595225.cdn.sohucs.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808998&t=358c52bfaa044bf5b6ca51935de10c42",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F9f569629c4dec5ed1b603982058c6853607b1f0af685e-PcenmQ_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281144&t=e56fa317e4de55b22bcb49f94cc042a7",
    };
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.hello);
        findViewById(R.id.onClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));

            }
        });
        findViewById(R.id.twoClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity3.class));
            }
        });

        findViewById(R.id.ThreeClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity6.class));
            }
        });
        findViewById(R.id.clickToShowTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Title title= new Title();
                mTextView.setText(title.getString());
            }
        });

        findViewById(R.id.hotFixBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启热更新
                //get classLoader
                //1,复制 文件
                //2,替换classLoader
                //3,
                File apk = new File(getCacheDir() + "/hotfix.apk");
                if (!apk.exists()) {
                    try (Source source = Okio.source(getAssets().open("apk/hotfix.apk")); BufferedSink sink = Okio.buffer(Okio.sink(apk))) {
                        sink.writeAll(source);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    ClassLoader originClassLoader = getClassLoader();
                    DexClassLoader classLoader = new DexClassLoader(apk.getPath(), getCacheDir().getPath(), null, null);
                    Class loaderClass = BaseDexClassLoader.class;

                    Field pathListField = loaderClass.getDeclaredField("pathList");
                    pathListField.setAccessible(true);
                    Object pathListObject = pathListField.get(classLoader);

                    Class pathListClass = pathListObject.getClass();
                    Field dexElementsFiled = pathListClass.getDeclaredField("dexElements");
                    dexElementsFiled.setAccessible(true);
                    Object dexElementsObject = dexElementsFiled.get(pathListObject);
                    Object originalPathListObject = dexElementsFiled.get(originClassLoader);

                    pathListField.set(originalPathListObject, dexElementsObject);// 自动替换
                    // originalLoader .pathList.dexElement= classLoader .pathList.dexElement
                    //                originClassLoader.pathList.dexElements += classLoader.pathList.dexElements;
                    //Object originalDexElementObject = dexElementsFiled.get(originClassLoader);
                    // 新值加在旧值上
                    //originClassLoader.pathList.dexElements[3]=originClassLoader.pathList.dexElement[2]
                    //originClassLoader.pathList.dexElements[2]=originClassLoader.pathList.dexElement[1]
                    //originClassLoader.pathList.dexElements[1]=originClassLoader.pathList.dexElement[0]
                    //originClassLoader.pathList.dexElements[0]=newDex;



                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }
        });

    }
}