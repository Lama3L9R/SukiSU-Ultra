# SukiSU - Zako Edition!

**简体中文** | [English](README-en.md) | [日本語](README-ja.md)

基于 [KernelSU](https://github.com/tiann/KernelSU) 的安卓设备 root 解决方案

**实验性! 使用风险自负!**

> 这是非官方分支，保留所有权利 [@tiann](https://github.com/tiann)
>
> 还有上游 [@ShirkNeko](https://github.com/ShirkNeko/SukiSU-Ultra)
>
> 定期有空同步上游改动，外加一些我自己的私货
> 不打算独立出去，只是 SukiSU 罢了
>

## 如何添加

在内核源码的根目录下执行以下命令：

使用 susfs-dev 分支（已集成susfs，带非GKI设备的支持）

**susfs-dev脚本还没改，别急哈**

```
curl -LSs "https://raw.githubusercontent.com/ShirkNeko/SukiSU-Ultra/main/kernel/setup.sh" | bash -s susfs-dev
```


使用 zako 分支
```
curl -LSs "https://raw.githubusercontent.com/Lama3L9r/SukiSU-Zako/zako/kernel/setup.sh" | bash -s zako
```

## 如何集成 susfs

改了一堆内核内容没合到susfs分支，暂时不支持，人话：没做完别急

## 钩子方法

- 此部分引用自 [rsuntk 的钩子方法](https://github.com/rsuntk/KernelSU)

1. **KPROBES 钩子：**
    - 用于可加载内核模块 (LKM)
    - GKI 2.0 内核的默认钩子方法
    - 需要 `CONFIG_KPROBES=y`

2. **手动钩子：**
    - 标准的 KernelSU 钩子：https://kernelsu.org/guide/how-to-integrate-for-non-gki.html#manually-modify-the-kernel-source
    - backslashxx 的 syscall 手动钩子：https://github.com/backslashxx/KernelSU/issues/5
    - 非 GKI 内核的默认挂钩方法
    - 需要 `CONFIG_KSU_MANUAL_HOOK=y`

## KPM 支持

- 我们基于 KernelPatch 去掉了和 KSU 重复的功能，仅保留了 KPM 支持
- 我们将会引入更多的兼容 APatch 的函数来确保 KPM 功能的完整性

开源地址: https://github.com/ShirkNeko/SukiSU_KernelPatch_patch

KPM 模板地址: https://github.com/udochina/KPM-Build-Anywhere

KPM模板地址: https://github.com/udochina/KPM-Build-Anywhere


## 使用方法

无技术支持，自己编译自己搞，炸了我不负责


## 特点

1. 基于内核的 `su` 和 root 访问管理
2. 基于 5ec1cff 的 [Magic Mount](https://github.com/5ec1cff/KernelSU) 的模块系统
3. [App Profile](https://kernelsu.org/guide/app-profile.html)：将 root 权限锁在笼子里
4. 恢复对非 GKI 2.0 内核的支持
5. 更多自定义功能
6. 对 KPM 内核模块的支持
7. Zako~ Zako~ Zako~
8. WIP 禁止过检测：强制给所有检测器 APP 挂载 SU 且不可关闭
9. 我的个人私货

## 许可证

- `kernel` 目录下的文件是 [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)。
- 除 `kernel` 目录外，所有其他部分均为 [GPL-3.0 或更高版本](https://www.gnu.org/licenses/gpl-3.0.html)。

我不懂许可证，在不与原始许可证不冲突的前提下，所有新增代码按照 Anti-996 许可证走。
若出现冲突条例，以源许可证为准。

## 赞助名单 (上游的，也欢迎通过 GitHub 赞助我)
- [Ktouls](https://github.com/Ktouls) 非常感谢你给我带来的支持
- [zaoqi123](https://github.com/zaoqi123) 请我喝奶茶也不错
- [wswzgdg](https://github.com/wswzgdg) 非常感谢对此项目的支持
- [yspbwx2010](https://github.com/yspbwx2010) 非常感谢
- [DARKWWEE](https://github.com/DARKWWEE) 感谢老哥的 100 USDT


这是上游的赞助名单，如果你喜欢 ZakoSU，可以考虑优先赞助上游项目 因为 99%+ 代码都是上游写的。
我只是改名 + 塞私货而已。如果你真的很像给我赞助，可以通过 Github 给我赞助。

如果以上名单没有你的名称，我会及时更新，再次感谢大家的支持

## 贡献

- [KernelSU](https://github.com/tiann/KernelSU)：原始项目
- [MKSU](https://github.com/5ec1cff/KernelSU)：使用的项目
- [RKSU](https://github.com/rsuntk/KernelsU)：使用该项目的 kernel 对非GKI设备重新进行支持
- [susfs4ksu](https://gitlab.com/simonpunk/susfs4ksu)：使用的 susfs 文件系统
- [kernel-assisted-superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/)：KernelSU 的构想
- [Magisk](https://github.com/topjohnwu/Magisk)：强大的 root 工具
- [genuine](https://github.com/brevent/genuine/)：APK v2 签名验证
- [Diamorphine](https://github.com/m0nad/Diamorphine)：一些 rootkit 技能
- [KernelPatch](https://github.com/bmax121/KernelPatch): KernelPatch 是 APatch 实现内核模块的关键部分
- [SukiSU-Ultra](https://github.com/ShirkNeko/SukiSU-Ultra): 上游
- [是我呢](https://github.com/Lama3L9R): 嗯。是我。
- [是我的小号呢](https://github.com/lamadaemon): 嗯。还是我。
