![](https://s1.ax1x.com/2020/08/14/dPNYx1.png)
> 虚拟实体解决方案。

# Adyeshach
Adyeshach 是免费的，但我们不提供 jar 文件，你可以通过以下步骤自行构建插件。

**Windows 平台**
```shell
gradlew.bat clean build
```

**macOS/Linux 平台**
```shell
./gradlew clean build
```

# Frontier Fork

本 Fork 包含如下改动：

## 公有 NPC 调整

+ 公有且持久化的 NPC 限制使用重复的 ID，且持久化文件以 `TYPE-ID` 命名。
+ 不再允许对 ID 进行二次修改。
+ 不再支持 `rename` 指令。

## TraitSit 调整

+ 暴力移除无效的 Sit 载具