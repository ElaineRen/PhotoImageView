kotlin 基础总结:

修饰符：
1,var: 可变变量
  val: 只读变量，不可变，类似于java 的final 修饰符
访问性修饰符 open， final ，obstract
final: 在kotlin中 所有类和方法 默认都是final的，如果允许继承，需要加修饰符 open

data：声明数据类型，使用data 自动生成 get set toString hashcode equals等方法

2，变量声明： 变量名：变量类型 （eg 声明一个计数单位：count：Int）
3，方法 即函数的声明 以fun字段开始  函数名称紧随其后 括号中是参数列表，参数列表里面跟随 返回类型，返回类型和参数列表之间使用冒号分开


?????
为什么监听回调都是 object:XXX ？