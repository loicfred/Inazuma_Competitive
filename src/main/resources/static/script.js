

function isInteger(value) {
    return /^\d+$/.test(value); // checks if the string is a whole positive integer
}

function showMessageModal(message, color) {
    const modal = document.getElementById("notice-messageModal");
    const messageText = document.getElementById("notice-messageText");
    const closeModal = document.getElementById("notice-closeModal");
    messageText.textContent = message;
    messageText.style.color = color;
    modal.style.display = "block";
    closeModal.addEventListener('click', () => {
        modal.style.display = "none";
    });
}

const commonItems = [
    {suffix: "ALL", label: "All"},
    {suffix: "IEGOGLX", label: "IEGOGLX"},
    {suffix: "IEGOCS", label: "IEGOCS"},
    {suffix: "IEGO1", label: "IEGO1"},
    {suffix: "IEVR", label: "IEVR"},
    {suffix: "IEVRBETA", label: "IEVRBETA"},
    {suffix: "IEGOSTR", label: "IEGOSTR"},
    {suffix: "IEGOSTRXTR", label: "IEGOSTRXTR2013"}
];


function generateSwitches(containerId, prefix, items, onchangeFunction) {
    const container = document.getElementById(containerId);
    items.forEach(item => {
        const name = prefix + "-" + item.suffix;
        const switchLabel = document.createElement('label');
        switchLabel.classList.add('miniswitch');
        switchLabel.innerHTML = `
        <input type="checkbox" name="${name}" id="${name}" onchange="${onchangeFunction}('${name}')">
        <span class="minislider round"></span>
      `;

        const label = document.createElement('label');
        label.setAttribute('for', name);
        label.style.paddingRight = '20px';
        label.className = 'IEStrike';
        label.innerText = item.label;
        if (item.label === "All") {
            label.style.borderRight = '1px solid #FFFFFF55';
            label.style.paddingRight = '5px';
        }
        container.appendChild(switchLabel);
        container.appendChild(label);
    });
    document.querySelector(`input[name='${prefix}-ALL']`).checked = true;
}

function writeCheckboxField(identifier, labelText, containerId, isChecked) {
    const checkedAttribute = isChecked ? 'checked' : '';
    const html = `
        <div style="display: flex; width: 100%; padding-right: 10px;">
            <label class="IEStrike" for="${identifier}">${labelText}</label>
            <label class="switch" style="margin-left: auto;">
                <input type="checkbox" name="${identifier}" id="${identifier}" ${checkedAttribute}>
                <span class="slider round"></span>
            </label>
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}

function writeTextField(identifier, labelText, maxLength, containerId, defaultValue = '') {
    const html = `
        <div class="form-group2">
            <label class="IEStrike" for="${identifier}">${labelText}</label>
            <input type="text" id="${identifier}" name="${identifier}" maxlength="${maxLength}" value="${defaultValue == null ? '' : defaultValue}">
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}

function writeColorField(identifier, labelText, containerId, defaultValue = '#000000') {
    const html = `
        <div style="display: flex; align-items: center; width: 100%; margin-bottom: 20px; padding-right: 10px;">
            <label class="IEStrike" for="${identifier}">${labelText}</label>
            <input type="color" id="${identifier}" name="${identifier}" value="${defaultValue == null ? '' : defaultValue}" style="margin-left: auto;">
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}

function writeDateField(identifier, labelText, containerId, defaultValue = '') {
    const html = `
        <div class="form-group2">
            <label class="IEStrike" for="${identifier}">${labelText}</label>
            <input type="date" id="${identifier}" name="${identifier}" value="${defaultValue}">
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}

function writeTextAreaField(fieldname, identifier, labelText, containerId, maxLength = 256, rows = 4, defaultValue = '') {
    const html = `
        <div class="form-group2">
            <label class="IEStrike" for="${identifier}">${labelText}</label>
            <textarea id="${identifier}" name="${identifier}" maxlength="${maxLength}" rows="${rows}">${defaultValue}</textarea>
        </div>
    `;
    document.getElementById(containerId).insertAdjacentHTML('beforeend', html);
}