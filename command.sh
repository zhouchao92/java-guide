# 栈信息输出到文件
jstack [pid] > stack.txt

# 堆信息
jmap heap

# gc 回收
jstat -gcutil [pid] | head -n [lines]