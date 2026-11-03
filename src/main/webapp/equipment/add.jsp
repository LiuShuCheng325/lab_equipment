<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- 权限校验统一由 EquipmentServlet#requireAdmin 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="equipment" scope="page"/>
<c:set var="pageTitle" value="新增设备" scope="page"/>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${fn:escapeXml(pageTitle)} - 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="app-shell">
    <%@ include file="/common/sidebar.jsp" %>

    <div class="main">
        <%@ include file="/common/navbar.jsp" %>

        <div class="page">
            <div class="page-head">
                <div>
                    <h1>${fn:escapeXml(pageTitle)}</h1>
                    <p class="desc">录入一台新的实验室设备，保存后即可被借用与检索。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--ghost">返回设备一览</a>
                </div>
            </div>

            <div class="form-layout">
                <%-- ============ 表单 ============ --%>
                <section class="panel">
                    <div class="panel__head">
                        <h2>设备信息</h2>
                        <span class="sub">带 <span class="req">*</span> 为必填项</span>
                    </div>

                    <div class="panel__body">
                        <c:if test="${not empty errMsg}">
                            <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
                        </c:if>

                        <form id="addEquipmentForm" method="post" action="${ctx}/equipment">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="img" id="imgPath" value="">

                            <div class="field">
                                <label for="eqName">设备名称 <span class="req">*</span></label>
                                <input id="eqName" type="text" name="eqName" class="form-control"
                                       maxlength="60" required placeholder="例：紫外可见分光光度计">
                                <span class="input-tip">最长 60 个字符，请填写标准设备名称</span>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="model">设备型号 <span class="req">*</span></label>
                                    <input id="model" type="text" name="model" class="form-control"
                                           maxlength="40" required placeholder="例：UV-2600">
                                </div>
                                <div class="form-group">
                                    <label for="eqType">设备类别 <span class="req">*</span></label>
                                    <input id="eqType" type="text" name="eqType" class="form-control"
                                           maxlength="30" required
                                           placeholder="例：光学仪器 / 称量设备 / 电子测量">
                                    <span class="input-tip">类别一致的设备会在统计中合并显示</span>
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="stock">采购总库存 <span class="req">*</span></label>
                                    <input id="stock" type="number" name="stock" class="form-control"
                                           min="0" max="9999" required placeholder="例：10">
                                    <span class="input-tip">新增后当前可用数量自动等于总库存</span>
                                </div>
                                <div class="form-group">
                                    <label for="location">存放位置 <span class="req">*</span></label>
                                    <input id="location" type="text" name="location" class="form-control"
                                           maxlength="50" required placeholder="例：一号实验楼 302 仪器柜 B 区">
                                </div>
                            </div>

                            <div class="field">
                                <label for="imgFile">设备图片</label>
                                <input id="imgFile" type="file" class="form-control"
                                       accept="image/jpeg,image/png,image/gif,image/webp">
                                <span class="input-tip">选填，支持 jpg / png / gif / webp，单张不超过 2MB；不上传则列表显示占位图标</span>

                                <%-- 选中图片后本地即时预览，未选择时整块隐藏 --%>
                                <div class="img-preview" id="imgPreview" hidden>
                                    <img id="imgPreviewImg" src="" alt="设备图片预览">
                                    <div class="img-preview__bar">
                                        <span class="muted" id="imgPreviewName"></span>
                                        <button type="button" class="btn btn--ghost btn--sm" id="imgClear">移除图片</button>
                                    </div>
                                </div>
                            </div>

                            <div class="field">
                                <label for="description">设备功能描述</label>
                                <textarea id="description" name="description" class="form-control" rows="4"
                                          maxlength="300"
                                          placeholder="选填，填写设备用途、操作规程与使用注意事项"></textarea>
                                <span class="input-tip">最长 300 个字符，会展示在设备详情页</span>
                            </div>

                            <div class="actions" style="margin-top:24px">
                                <button type="submit" class="btn btn--primary btn--lg">确认新增</button>
                                <a href="${ctx}/equipment?action=list" class="btn btn--ghost btn--lg">取消</a>
                            </div>
                        </form>
                    </div>
                </section>

                <%-- ============ 须知 ============ --%>
                <div class="stack">
                    <div class="note-card">
                        <div class="note-card__title">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 3 2 8l10 5 10-5-10-5z"/>
                                <path d="m2 16 10 5 10-5M2 12l10 5 10-5"/>
                            </svg>
                            录入须知
                        </div>
                        <ol>
                            <li>带 <span class="req">*</span> 为必填项，缺项无法保存。</li>
                            <li>新增设备默认状态为「正常」，可立即被申请借用。</li>
                            <li>总库存填写后，可用数量自动同步为相同数值。</li>
                            <li>图片选填，支持 jpg / png；未上传时使用类别占位图标。</li>
                            <li>类别名称请保持统一，便于统计与筛选。</li>
                        </ol>
                    </div>

                    <section class="panel">
                        <div class="panel__head">
                            <h2>操作者</h2>
                        </div>
                        <div class="panel__body">
                            <dl class="detail-grid">
                                <dt>录入人</dt>
                                <dd>${fn:escapeXml(user.name)}</dd>
                                <dt>角色</dt>
                                <dd>${user.role == 1 ? '管理员' : '借用用户'}</dd>
                            </dl>
                            <p class="input-tip">新增记录会同步计入设备台账与类别统计。</p>
                        </div>
                    </section>
                </div>
            </div>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>
<script src="${ctx}/js/app.js?v=4.3"></script>
<script>
    /* 图片先异步上传拿到相对路径，再随表单一起提交；无图片则跳过 */
    (function () {
        var form = document.getElementById('addEquipmentForm');
        if (!form) {
            return;
        }
        var stockInput = document.getElementById('stock');
        var imgFile = document.getElementById('imgFile');
        var imgPathInput = document.getElementById('imgPath');
        var textInputs = document.querySelectorAll('#addEquipmentForm input[type="text"], #addEquipmentForm textarea');
        var submitting = false;

        for (var i = 0; i < textInputs.length; i++) {
            textInputs[i].addEventListener('blur', function () {
                this.value = this.value.trim();
            });
        }

        // ===== 图片本地预览：选中即用 FileReader 回显，不必等上传完成 =====
        var previewBox = document.getElementById('imgPreview');
        var previewImg = document.getElementById('imgPreviewImg');
        var previewName = document.getElementById('imgPreviewName');
        var clearBtn = document.getElementById('imgClear');
        var MAX_IMG = 2 * 1024 * 1024;

        function showPreview(file) {
            if (!previewBox || !previewImg) {
                return;
            }
            if (!file) {
                previewBox.hidden = true;
                previewImg.removeAttribute('src');
                return;
            }
            var reader = new FileReader();
            reader.onload = function (ev) {
                previewImg.src = ev.target.result;
                previewBox.hidden = false;
                if (previewName) {
                    previewName.textContent = file.name;
                }
            };
            reader.readAsDataURL(file);
        }

        if (imgFile) {
            imgFile.addEventListener('change', function () {
                var file = this.files && this.files[0];
                if (file && file.size > MAX_IMG) {
                    alert('图片不能超过 2MB，请压缩后重试！');
                    this.value = '';
                    showPreview(null);
                    return;
                }
                showPreview(file);
            });
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', function () {
                if (imgFile) {
                    imgFile.value = '';
                }
                if (imgPathInput) {
                    imgPathInput.value = '';
                }
                showPreview(null);
            });
        }

        form.addEventListener('submit', function (e) {
            var total = parseInt(stockInput.value, 10);
            if (isNaN(total) || total < 0) {
                e.preventDefault();
                alert('总库存必须是不小于 0 的整数！');
                stockInput.focus();
                return;
            }
            if (submitting) {
                return;
            }
            if (!imgFile || imgFile.files.length === 0) {
                return;
            }
            var file = imgFile.files[0];
            if (file.size > MAX_IMG) {
                e.preventDefault();
                alert('图片不能超过 2MB！');
                return;
            }
            e.preventDefault();
            submitting = true;
            var fd = new FormData();
            fd.append('uploadImg', file);
            fetch('${ctx}/upload', {method: 'POST', body: fd, credentials: 'same-origin'})
                .then(function (res) {
                    return res.text();
                })
                .then(function (text) {
                    imgPathInput.value = (text || '').trim();
                    submitting = false;
                    form.submit();
                })
                .catch(function () {
                    alert('图片上传失败，已改为不带图片提交。');
                    imgPathInput.value = '';
                    submitting = false;
                    form.submit();
                });
        });
    })();
</script>
</body>
</html>
