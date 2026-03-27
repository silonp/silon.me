// contact-form.js
// Author: Peter Šilon

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("contactForm");
    const button = form.querySelector("button");
    const spinner = button.querySelector(".spinner-border");
    const messageBox = document.getElementById("formMessage");

    form.addEventListener("submit", (e) => {
        e.preventDefault();

        // Show spinner + disable button
        button.disabled = true;
        spinner.classList.remove("d-none");

        grecaptcha.enterprise.ready(async () => {
            const token = await grecaptcha.enterprise.execute(
                "6LeJy5ksAAAAAFITjXdqqyAON8p0MNBpj65wvK8i",
                { action: "LOGIN" }
            );

            const formData = new FormData(form);
            formData.append("g-recaptcha-response", token);

            try {
                const response = await fetch("/api/contact", {
                    method: "POST",
                    body: formData,
                });

                // Parse the JSON response from the backend
                const result = await response.json();

                if (response.ok) {
                    messageBox.innerHTML = `<span class='text-success'>${result.message}</span>`;
                    form.reset();
                } else {
                    const errorMsg = result.error || "Something went wrong.";
                    messageBox.innerHTML = `<span class='text-danger'>${errorMsg}</span>`;
                }
            } catch (error) {
                messageBox.innerHTML =
                    "<span class='text-danger'>Network error. Please try again later.</span>";
            } finally {
                button.disabled = false;
                spinner.classList.add("d-none");

                // We haven't rendered it, so we don't reset the widget.
                // grecaptcha.enterprise.reset();
            }
        });
    });
});