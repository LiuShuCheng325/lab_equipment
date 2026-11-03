<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- 权限校验统一由 EquipmentServlet#requireAdmin 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="equip" value="${requestScope.equipment}" scope="page"/>
<c:set var="activeNav" value="equipment" scope="page"/>
<c:set var="pageTitle" value="编辑设备" scope="page"/>

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
                <p class="desc">修改设备台账信息，库存与状态调整会立即影响借用申请。</p>
            </div>
            <div class="actions">
                <a href="${ctx}/equipment?action=detail&id=${equip.id}" class="btn btn--ghost">查看设备详情</a>
                <a href="${ctx}/equipment?action=list" class="btn btn--ghost">返回设备一览</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty equip}">
                <section class="panel">
                    <div class="panel__body">
                        <div class="empty">
                            <div class="empty__art">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"
                                     stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                                    <path d="M3.27 6.96 12 12.01l8.73-5.05M12 22.08V12"/>
                                </svg>
                            </div>
                            <div class="empty__title">设备不存在或已被删除</div>
                            <div class="empty__desc">请返回设备一览重新选择要编辑的设备</div>
                            <a href="${ctx}/equipment?action=list" class="btn btn--primary empty__action">返回设备一览</a>
                        </div>
                    </div>
                </section>
            </c:when>
            <c:otherwise>
                <div class="form-layout">
                    <%-- ============ 表单 ============ --%>
                    <section class="panel">
                        <div class="panel__head">
                            <h2>设备信息</h2>
                            <span class="sub">设备编号 #${equip.id}</span>
                        </div>

                        <div class="panel__body">
                            <c:if test="${not empty errMsg}">
                                <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
                            </c:if>

                            <form id="editEquipmentForm" method="post" action="${ctx}/equipment">
                                <input type="hidden" name="action" value="edit">
                                <input type="hidden" name="id" value="${equip.id}">
                                <input type="hidden" name="img" id="imgPath"
                                       value="${fn:escapeXml(equip.img)}">

                                <div class="field">
                                    <label for="imgFile">设备图片</label>
                                    <input id="imgFile" type="file" class="form-control"
                                           accept="image/jpeg,image/png,image/gif,image/webp">
                                    <span class="input-tip">选填，支持 jpg / png / gif / webp，单张不超过 2MB；
                                        不重新选择则保留原图片</span>

                                    <%-- 图片预览：已存在图片时默认回显，重新选择后即时替换 --%>
                                    <div class="img-preview" id="imgPreview" ${empty equip.img ? 'hidden' : ''}>
                                        <img id="imgPreviewImg"
                                             src="${empty equip.img ? '' : ctx}${fn:escapeXml(equip.img)}"
                                             alt="设备图片预览">
                                        <div class="img-preview__bar">
                                            <span class="muted" id="imgPreviewName">
                                                ${empty equip.img ? '' : '当前图片'}
                                            </span>
                                            <button type="button" class="btn btn--ghost btn--sm" id="imgClear">移除图片</button>
                                        </div>
                                    </div>
                                </div>

                                <div class="field">
                                    <label for="eqName">设备名称 <span class="req">*</span></label>
                                    <input id="eqName" type="text" name="eqName" class="form-control"
                                           maxlength="60" required
                                           value="${fn:escapeXml(equip.eqName)}">
                                </div>

                                <div class="form-row">
                                    <div class="form-group">
                                        <label for="model">设备型号 <span class="req">*</span></label>
                                        <input id="model" type="text" name="model" class="form-control"
                                               maxlength="40" required
                                               value="${fn:escapeXml(equip.model)}">
                                    </div>
                                    <div class="form-group">
                                        <label for="eqType">设备类别 <span class="req">*</span></label>
                                        <input id="eqType" type="text" name="eqType" class="form-control"
                                               maxlength="30" required
                                               value="${fn:escapeXml(equip.eqType)}">
                                    </div>
                                </div>

                                <div class="form-row">
                                    <div class="form-group">
                                        <label for="stock">总库存数量 <span class="req">*</span></label>
                                        <input id="stock" type="number" name="stock" class="form-control"
                                               min="0" max="9999" required value="${equip.stock}">
                                    </div>
                                    <div class="form-group">
                                        <label for="available">当前可用数量 <span class="req">*</span></label>
                                        <input id="available" type="number" name="available"
                                               class="form-control" min="0" max="9999" required
                                               value="${equip.available}">
                                        <span class="input-tip">可用数量不可为负，且不能超过总库存</span>
                                    </div>
                                </div>

                                <div class="field">
                                    <label for="location">存放位置 <span class="req">*</span></label>
                                    <input id="location" type="text" name="location" class="form-control"
                                           maxlength="50" required
                                           value="${fn:escapeXml(equip.location)}">
                                </div>

                                <div class="field">
                                    <label for="description">设备描述</label>
                                    <textarea id="description" name="description" class="form-control"
                                              rows="4" maxlength="300">${fn:escapeXml(equip.remark)}</textarea>
                                    <span class="input-tip">最长 300 个字符，填写设备功能与使用注意事项</span>
                                </div>

                                <div class="field">
                                    <label for="status">设备状态 <span class="req">*</span></label>
                                    <select id="status" name="status" class="select" required>
                                        <option value="0" ${equip.status == 0 ? 'selected' : ''}>正常（可借用）</option>
                                        <option value="1" ${equip.status == 1 ? 'selected' : ''}>维修中（禁止借用）</option>
                                        <option value="2" ${equip.status == 2 ? 'selected' : ''}>已报废（禁止借用）</option>
                                    </select>
                                    <span class="input-tip">状态改为「维修中 / 已报废」后，学生将无法申请借用</span>
                                </div>

                                <div class="actions" style="margin-top:24px">
                                    <button type="submit" class="btn btn--primary btn--lg">保存修改</button>
                                    <a href="${ctx}/equipment?action=detail&id=${equip.id}"
                                       class="btn btn--ghost btn--lg">取消</a>
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
                                    <circle cx="12" cy="12" r="9"/>
                                    <path d="M12 16v-4M12 8h.01"/>
                                </svg>
                                编辑须知
                            </div>
                            <ol>
                                <li>可用数量不得大于设备总库存，否则无法保存。</li>
                                <li>状态调整为「维修中 / 已报废」后，学生端将无法申请借用该设备。</li>
                                <li>库存调整不会自动回填进行中的借用记录，请谨慎修改。</li>
                                <li>带 <span class="req">*</span> 为必填项。</li>
                            </ol>
                        </div>

                        <section class="panel">
                            <div class="panel__head">
                                <h2>当前库存</h2>
                            </div>
                            <div class="panel__body">
                                <div class="hbar-list">
                                    <div class="hbar">
                                        <div class="hbar__top">
                                            <span class="hbar__name">可用 / 总库存</span>
                                            <span class="hbar__value">${equip.available} / ${equip.stock}</span>
                                        </div>
                                        <div class="hbar__track">
                                            <i class="hbar__fill bar--ok"
                                               style="width:${equip.stock > 0 ? (equip.available * 100 / equip.stock) : 0}%"></i>
                                        </div>
                                    </div>
                                </div>
                                <p class="input-tip">累计借出 ${empty equip.borrowCount ? 0 : equip.borrowCount} 次。</p>
                            </div>
                        </section>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>
<script src="${ctx}/js/app.js?v=4.3"></script>
<script>
    /* 编辑页：库存联动校验，避免提交后才报错 */
    (function () {
        var form = document.getElementById('editEquipmentForm');
        if (!form) {
            return;
        }
        var stockInput = document.getElementById('stock');
        var availableInput = document.getElementById('available');
        var imgFile = document.getElementById('imgFile');
        var imgPathInput = document.getElementById('imgPath');
        var textInputs = document.querySelectorAll('#editEquipmentForm input[type="text"], #editEquipmentForm textarea');
        var MAX_IMG = 2 * 1024 * 1024;
        var submitting = false;

        for (var i = 0; i < textInputs.length; i++) {
            textInputs[i].addEventListener('blur', function () {
                this.value = this.value.trim();
            });
        }

        // ===== 图片预览：支持回显当前图片 + 重新选择后本地即时替换 =====
        var previewBox = document.getElementById('imgPreview');
        var previewImg = document.getElementById('imgPreviewImg');
        var previewName = document.getElementById('imgPreviewName');
        var clearBtn = document.getElementById('imgClear');

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

        stockInput.addEventListener('input', function () {
            var total = parseInt(stockInput.value, 10);
            var avail = parseInt(availableInput.value, 10);
            if (!isNaN(total) && !isNaN(avail) && avail > total) {
                availableInput.value = total;
            }
        });

        form.addEventListener('submit', function (e) {
            var total = parseInt(stockInput.value, 10);
            var avail = parseInt(availableInput.value, 10);
            if (isNaN(total) || isNaN(avail)) {
                e.preventDefault();
                alert('库存、可用数量必须输入合法数字！');
                return;
            }
            if (avail > total) {
                e.preventDefault();
                alert('可用数量不能大于设备总库存！');
                availableInput.focus();
                return;
            }

            // 选了新图片：先异步上传拿到路径写回隐藏域，再提交表单；
            // 未选新图片则直接提交，隐藏域保留原有图片路径。
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
            if (submitting) {
                return;
            }
            submitting = true;
            var fd = new FormData();
            fd.append('uploadImg', file);
            fetch('${ctx}/upload', {method: 'POST', body: fd, credentials: 'same-origin'})
                .then(function (res) {
                    return res.text();
                })
                .then(function (text) {
                    var path = (text || '').trim();
                    if (path) {
                        imgPathInput.value = path;
                    }
                    submitting = false;
                    form.submit();
                })
                .catch(function () {
                    alert('图片上传失败，已保留原图片提交。');
                    submitting = false;
                    form.submit();
                });
        });
    })();
</script>
</body>
</html>
