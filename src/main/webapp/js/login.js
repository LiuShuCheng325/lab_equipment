const loginRules = {
    username: /^[a-zA-Z0-9]{4,16}$/,
    password: /^.{6,20}$/
};
function showTip(id) {
    document.getElementById(id).style.display = "block";
}
function hideTip(id) {
    document.getElementById(id).style.display = "none";
}
function validateLogin() {
    let pass = true;
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    hideTip("userTip");
    hideTip("pwdTip");
    document.querySelectorAll(".form-control").forEach(el => el.classList.remove("error"));
    if (!loginRules.username.test(username)) {
        showTip("userTip");
        document.getElementById("username").classList.add("error");
        pass = false;
    }
    if (!loginRules.password.test(password)) {
        showTip("pwdTip");
        document.getElementById("password").classList.add("error");
        pass = false;
    }
    document.getElementById("username").value = username;
    document.getElementById("password").value = password;
    return pass;
}

// 等待页面全部DOM加载完毕再绑定事件，防止元素未找到
window.addEventListener("DOMContentLoaded", function(){
    document.querySelectorAll(".form-control").forEach(input => {
        input.addEventListener("input", function () {
            this.classList.remove("error");
            const tipId = this.id === "username" ? "userTip" : "pwdTip";
            hideTip(tipId);
        })
    })
});