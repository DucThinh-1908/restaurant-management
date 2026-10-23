document.addEventListener("DOMContentLoaded", () => {
  const toggle = document.querySelector(".menu-toggle");
  const nav = document.querySelector(".main-nav");
  if (toggle && nav) {
    toggle.addEventListener("click", () => {
      const isOpen = nav.classList.toggle("open");
      toggle.setAttribute("aria-expanded", String(isOpen));
    });
  }

  document.querySelectorAll("[data-demo-form]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const notice = form.querySelector(".notice");
      if (notice) {
        notice.textContent = "Cảm ơn bạn! Đây là giao diện minh họa; hãy kết nối biểu mẫu với backend để gửi dữ liệu thật.";
        notice.classList.add("show");
      }
      form.reset();
    });
  });
});
