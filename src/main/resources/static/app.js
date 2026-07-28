const API_BASE = '/api';
const appDiv = document.getElementById('app');

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
}

async function fetchAPI(endpoint, method = 'GET', body = null) {
    try {
        const options = { method, headers: { 'Content-Type': 'application/json' } };
        if (body) options.body = JSON.stringify(body);
        
        let url = endpoint;
        if (!endpoint.startsWith('http')) {
            if (endpoint.startsWith('/api')) {
                url = endpoint;
            } else {
                url = `${API_BASE}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
            }
        }
        
        const res = await fetch(url, options);
        const text = await res.text();
        if (!res.ok) throw new Error(text || res.statusText);
        return text ? JSON.parse(text) : null;
    } catch (e) {
        showToast('Error: ' + e.message);
        throw e;
    }
}

// Router
window.addEventListener('popstate', router);
document.body.addEventListener('click', e => {
    if (e.target.matches('a[href^="/"]')) {
        e.preventDefault();
        history.pushState(null, '', e.target.getAttribute('href'));
        router();
    }
});

function router() {
    let path = window.location.pathname;
    if (path === '/') path = '/api';

    if (path === '/api') renderHome();
    else if (path === '/users') renderUsers();
    else if (path === '/lenders') renderLenders();
    else if (path === '/loans') renderLoans();
    else if (path === '/lms') renderLMS();
    else if (path === '/lms/inquire') renderInquire();
    else if (path === '/lms/apply') renderApply();
    else if (path.startsWith('/lms/')) renderLoanDetails(path.split('/')[2]);
    else appDiv.innerHTML = '<h2>404 Not Found</h2><a href="/api" class="btn">Go Home</a>';
}

function renderHome() {
    appDiv.innerHTML = `
        <h2>Core Modules</h2>
        <div class="grid-4">
            <a href="/users" class="dashboard-btn">👤 Users</a>
            <a href="/lenders" class="dashboard-btn">🏦 Lenders</a>
            <a href="/loans" class="dashboard-btn">📦 Loan Products</a>
            <a href="/lms" class="dashboard-btn">💳 LMS</a>
        </div>
    `;
}

// ---- USERS ----
async function renderUsers() {
    appDiv.innerHTML = `<h2>Loading Users...</h2>`;
    const users = await fetchAPI('/users');
    let editingUserId = null;
    appDiv.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
            <h2>Users Management</h2>
            <a href="/api" class="btn btn-secondary">Home</a>
        </div>
        <div class="card">
            <h3 id="u_form_title">Add User</h3>
            <div class="grid-4 form-group">
                <input type="text" id="u_name" placeholder="Name">
                <input type="text" id="u_address" placeholder="Address">
                <input type="text" id="u_phone" placeholder="Phone">
                <input type="text" id="u_kyc" placeholder="KYC">
            </div>
            <button class="btn" id="u_submit_btn" onclick="submitUser()">Save User</button>
            <button class="btn btn-danger" style="display:none;" id="u_cancel_btn" onclick="cancelEditUser()">Cancel</button>
        </div>
        <div class="card">
            <table>
                <tr><th>ID</th><th>Name</th><th>Address</th><th>Phone</th><th>KYC</th><th>Actions</th></tr>
                ${users.map(u => `
                    <tr>
                        <td>${u.userId}</td><td>${u.userName}</td><td>${u.userAddress}</td>
                        <td>${u.userPhone || '-'}</td><td>${u.userKycDetails || '-'}</td>
                        <td>
                            <button class="btn" onclick="editUser(${u.userId}, '${u.userName}', '${u.userAddress}', '${u.userPhone || ''}', '${u.userKycDetails || ''}')">Edit</button>
                            <button class="btn btn-danger" onclick="deleteUser(${u.userId})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </table>
        </div>
    `;
    
    window.editUser = function(id, name, address, phone, kyc) {
        editingUserId = id;
        document.getElementById('u_form_title').textContent = 'Edit User #' + id;
        document.getElementById('u_name').value = name;
        document.getElementById('u_address').value = address;
        document.getElementById('u_phone').value = phone;
        document.getElementById('u_kyc').value = kyc;
        document.getElementById('u_submit_btn').textContent = 'Update User';
        document.getElementById('u_cancel_btn').style.display = 'inline-block';
    };
    
    window.cancelEditUser = function() {
        editingUserId = null;
        renderUsers();
    };

    window.submitUser = async function() {
        const data = {
            userName: document.getElementById('u_name').value,
            userAddress: document.getElementById('u_address').value,
            userPhone: document.getElementById('u_phone').value,
            userKycDetails: document.getElementById('u_kyc').value
        };
        if (editingUserId) {
            await fetchAPI(`/users/${editingUserId}`, 'PUT', data);
            showToast('User updated!');
        } else {
            await fetchAPI('/users', 'POST', data);
            showToast('User created!');
        }
        editingUserId = null;
        renderUsers();
    };
}
window.deleteUser = async function(id) {
    if(confirm('Are you sure?')) { await fetchAPI(`/users/${id}`, 'DELETE'); showToast('Deleted'); renderUsers(); }
}

// ---- LENDERS ----
async function renderLenders() {
    appDiv.innerHTML = `<h2>Loading Lenders...</h2>`;
    const lenders = await fetchAPI('/lenders');
    let editingLenderId = null;
    appDiv.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
            <h2>Lenders Management</h2>
            <a href="/api" class="btn btn-secondary">Home</a>
        </div>
        <div class="card">
            <h3 id="l_form_title">Add Lender</h3>
            <div class="grid-4 form-group">
                <input type="text" id="l_name" placeholder="Name">
                <input type="text" id="l_contact" placeholder="Contact">
                <input type="text" id="l_details" placeholder="Details">
            </div>
            <button class="btn" id="l_submit_btn" onclick="submitLender()">Save Lender</button>
            <button class="btn btn-danger" style="display:none;" id="l_cancel_btn" onclick="cancelEditLender()">Cancel</button>
        </div>
        <div class="card">
            <table>
                <tr><th>ID</th><th>Name</th><th>Contact</th><th>Details</th><th>Actions</th></tr>
                ${lenders.map(l => `
                    <tr>
                        <td>${l.lenderId}</td><td>${l.lenderName}</td><td>${l.lenderContact}</td><td>${l.lenderDetails}</td>
                        <td>
                            <button class="btn" onclick="editLender(${l.lenderId}, '${l.lenderName}', '${l.lenderContact}', '${l.lenderDetails}')">Edit</button>
                            <button class="btn btn-danger" onclick="deleteLender(${l.lenderId})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </table>
        </div>
    `;
    
    window.editLender = function(id, name, contact, details) {
        editingLenderId = id;
        document.getElementById('l_form_title').textContent = 'Edit Lender #' + id;
        document.getElementById('l_name').value = name;
        document.getElementById('l_contact').value = contact;
        document.getElementById('l_details').value = details;
        document.getElementById('l_submit_btn').textContent = 'Update Lender';
        document.getElementById('l_cancel_btn').style.display = 'inline-block';
    };
    
    window.cancelEditLender = function() {
        editingLenderId = null;
        renderLenders();
    };

    window.submitLender = async function() {
        const data = {
            lenderName: document.getElementById('l_name').value,
            lenderContact: document.getElementById('l_contact').value,
            lenderDetails: document.getElementById('l_details').value
        };
        if (editingLenderId) {
            await fetchAPI(`/lenders/${editingLenderId}`, 'PUT', data);
            showToast('Lender updated!');
        } else {
            await fetchAPI('/lenders', 'POST', data);
            showToast('Lender created!');
        }
        editingLenderId = null;
        renderLenders();
    };
}
window.deleteLender = async function(id) {
    if(confirm('Are you sure?')) { await fetchAPI(`/lenders/${id}`, 'DELETE'); showToast('Deleted'); renderLenders(); }
}

// ---- LOANS (Products) ----
async function renderLoans() {
    appDiv.innerHTML = `<h2>Loading Loan Products...</h2>`;
    const loans = await fetchAPI('/loans');
    let editingLoanId = null;
    appDiv.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
            <h2>Loan Products</h2>
            <a href="/api" class="btn btn-secondary">Home</a>
        </div>
        <div class="card">
            <h3 id="lp_form_title">Add Product</h3>
            <div class="grid-4 form-group">
                <input type="number" id="lp_lender" placeholder="Lender ID">
                <select id="lp_type"><option value="PERSONAL">PERSONAL</option><option value="MERCHANT">MERCHANT</option></select>
                <input type="number" id="lp_amin" placeholder="Amount Min">
                <input type="number" id="lp_amax" placeholder="Amount Max">
                <input type="number" id="lp_imin" placeholder="Interest Min (%)">
                <input type="number" id="lp_imax" placeholder="Interest Max (%)">
                <input type="number" id="lp_tmin" placeholder="Time Min">
                <input type="number" id="lp_tmax" placeholder="Time Max">
            </div>
            <button class="btn" id="lp_submit_btn" onclick="submitLoan()">Save Product</button>
            <button class="btn btn-danger" style="display:none;" id="lp_cancel_btn" onclick="cancelEditLoan()">Cancel</button>
        </div>
        <div class="card">
            <table>
                <tr><th>ID</th><th>Lender</th><th>Type</th><th>Amt Min</th><th>Amt Max</th><th>Int Min (%)</th><th>Int Max (%)</th><th>Time Min</th><th>Time Max</th><th>Actions</th></tr>
                ${loans.map(l => `
                    <tr>
                        <td>${l.loanId}</td><td>${l.lenderId}</td><td>${l.typeOfLoan}</td>
                        <td>${l.loanAmountMin}</td><td>${l.loanAmountMax}</td>
                        <td>${l.loanInterestMin}</td><td>${l.loanInterestMax}</td>
                        <td>${l.loanTimeMin}</td><td>${l.loanTimeMax}</td>
                        <td>
                            <button class="btn" onclick="editLoan(${l.loanId}, ${l.lenderId}, '${l.typeOfLoan}', ${l.loanAmountMin}, ${l.loanAmountMax}, ${l.loanInterestMin}, ${l.loanInterestMax}, ${l.loanTimeMin}, ${l.loanTimeMax})">Edit</button>
                            <button class="btn btn-danger" onclick="deleteLoan(${l.loanId})">Delete</button>
                        </td>
                    </tr>
                `).join('')}
            </table>
        </div>
    `;
    
    window.editLoan = function(id, lenderId, type, aMin, aMax, iMin, iMax, tMin, tMax) {
        editingLoanId = id;
        document.getElementById('lp_form_title').textContent = 'Edit Product #' + id;
        document.getElementById('lp_lender').value = lenderId;
        document.getElementById('lp_type').value = type;
        document.getElementById('lp_amin').value = aMin;
        document.getElementById('lp_amax').value = aMax;
        document.getElementById('lp_imin').value = iMin;
        document.getElementById('lp_imax').value = iMax;
        document.getElementById('lp_tmin').value = tMin;
        document.getElementById('lp_tmax').value = tMax;
        document.getElementById('lp_submit_btn').textContent = 'Update Product';
        document.getElementById('lp_cancel_btn').style.display = 'inline-block';
    };
    
    window.cancelEditLoan = function() {
        editingLoanId = null;
        renderLoans();
    };

    window.submitLoan = async function() {
        const data = {
            lenderId: document.getElementById('lp_lender').value,
            typeOfLoan: document.getElementById('lp_type').value,
            loanAmountMin: document.getElementById('lp_amin').value,
            loanAmountMax: document.getElementById('lp_amax').value,
            loanInterestMin: document.getElementById('lp_imin').value,
            loanInterestMax: document.getElementById('lp_imax').value,
            loanTimeMin: document.getElementById('lp_tmin').value,
            loanTimeMax: document.getElementById('lp_tmax').value
        };
        if (editingLoanId) {
            await fetchAPI(`/loans/${editingLoanId}`, 'PUT', data);
            showToast('Product updated!');
        } else {
            await fetchAPI('/loans', 'POST', data);
            showToast('Product created!');
        }
        editingLoanId = null;
        renderLoans();
    };
}
window.deleteLoan = async function(id) {
    if(confirm('Are you sure?')) { await fetchAPI(`/loans/${id}`, 'DELETE'); showToast('Deleted'); renderLoans(); }
}

// ---- LMS Dashboard ----
async function renderLMS() {
    appDiv.innerHTML = `<h2>Loading Accounts...</h2>`;
    const accounts = await fetchAPI('/api/lms/accounts');
    appDiv.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>Loan Accounts (LMS)</h2>
            <div>
                <a href="/lms/inquire" class="btn">Inquire Loan</a>
                <a href="/lms/apply" class="btn btn-success">Apply for Loan</a>
                <a href="/api" class="btn btn-secondary">Home</a>
            </div>
        </div>
        <div class="card" style="margin-top: 1rem;">
            <table>
                <tr><th>LAN</th><th>User ID</th><th>Lender ID</th><th>Product ID</th><th>Amount</th><th>Rate</th><th>Time</th><th>Start Date</th><th>Status</th><th>Actions</th></tr>
                ${accounts.map(a => `
                    <tr>
                        <td>${a.lan}</td><td>${a.userId}</td><td>${a.lenderId}</td><td>${a.loanId}</td>
                        <td>${a.amount}</td><td>${a.rateOfInterest}%</td><td>${a.timePeriod}</td><td>${a.startDate || '-'}</td>
                        <td><span class="badge badge-${a.status}">${a.status}</span></td>
                        <td><a href="/lms/${a.lan}" class="btn">Details</a></td>
                    </tr>
                `).join('')}
            </table>
        </div>
    `;
}

// ---- LMS Inquire ----
function renderInquire() {
    appDiv.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>Inquire Available Loans</h2>
            <a href="/lms" class="btn btn-secondary">Back to LMS</a>
        </div>
        <div class="card grid-2 form-group" style="max-width: 600px;">
            <div><label>Amount</label><input type="number" id="iq_amount" value="25000"></div>
            <div><label>Rate of Interest</label><input type="number" id="iq_rate" value="10"></div>
            <div><label>Time Period</label><input type="number" id="iq_time" value="12"></div>
            <div><label>Loan Type</label><select id="iq_type"><option value="PERSONAL">PERSONAL</option><option value="MERCHANT">MERCHANT</option></select></div>
            <button class="btn" style="grid-column: span 2;" onclick="doInquire()">Search Loans</button>
        </div>
        <div id="inquire_results"></div>
    `;
}
window.doInquire = async function() {
    const resDiv = document.getElementById('inquire_results');
    resDiv.innerHTML = 'Searching...';
    const res = await fetchAPI('/api/lms/inquire', 'POST', {
        amount: document.getElementById('iq_amount').value,
        rateOfInterest: document.getElementById('iq_rate').value,
        timePeriod: document.getElementById('iq_time').value,
        typeOfLoan: document.getElementById('iq_type').value
    });
    if(res.length === 0) { resDiv.innerHTML = '<div class="card">No matching loans found.</div>'; return; }
    resDiv.innerHTML = `
        <div class="card">
            <h3>Matching Products</h3>
            <table>
                <tr><th>ID</th><th>Lender</th><th>Type</th><th>Amt Min</th><th>Amt Max</th><th>Int Min (%)</th><th>Int Max (%)</th><th>Time Min</th><th>Time Max</th></tr>
                ${res.map(l => `
                    <tr>
                        <td>${l.loanId}</td><td>${l.lenderId}</td><td>${l.typeOfLoan}</td>
                        <td>${l.loanAmountMin}</td><td>${l.loanAmountMax}</td>
                        <td>${l.loanInterestMin}</td><td>${l.loanInterestMax}</td>
                        <td>${l.loanTimeMin}</td><td>${l.loanTimeMax}</td>
                    </tr>
                `).join('')}
            </table>
        </div>
    `;
}

// ---- LMS Apply ----
function renderApply() {
    appDiv.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>Apply for Loan</h2>
            <a href="/lms" class="btn btn-secondary">Back to LMS</a>
        </div>
        <div class="card grid-2 form-group" style="max-width: 800px;">
            <div><label>User ID</label><input type="number" id="ap_user"></div>
            <div><label>Lender ID</label><input type="number" id="ap_lender"></div>
            <div><label>Loan Product ID</label><input type="number" id="ap_loan"></div>
            <div><label>Amount</label><input type="number" id="ap_amount"></div>
            <div><label>Rate of Interest</label><input type="number" id="ap_rate"></div>
            <div><label>Time Period</label><input type="number" id="ap_time"></div>
            <div><label>Application Date (Optional)</label><input type="date" id="ap_date"></div>
            <button class="btn btn-success" style="grid-column: span 2;" onclick="doApply()">Submit Application</button>
        </div>
        <div id="apply_result"></div>
    `;
}
window.doApply = async function() {
    const resDiv = document.getElementById('apply_result');
    const lanObj = await fetchAPI('/api/lms/apply', 'POST', {
        userId: document.getElementById('ap_user').value,
        lenderId: document.getElementById('ap_lender').value,
        loanId: document.getElementById('ap_loan').value,
        amount: document.getElementById('ap_amount').value,
        rateOfInterest: document.getElementById('ap_rate').value,
        timePeriod: document.getElementById('ap_time').value,
        applicationDate: document.getElementById('ap_date').value || undefined
    });
    showToast('Application Submitted!');
    resDiv.innerHTML = `
        <div class="card">
            <h3>Application Created: LAN ${lanObj.lan}</h3>
            <p>Status: <span class="badge badge-${lanObj.status}">${lanObj.status}</span></p>
            <br>
            <button class="btn btn-success" onclick="verifyLoan(${lanObj.lan})">Verify & Approve Now</button>
        </div>
    `;
}
window.verifyLoan = async function(lan) {
    await fetchAPI(`/api/lms/${lan}/verify?status=SUCCESS`, 'PUT');
    showToast('Loan Verified Successfully!');
    history.pushState(null, '', `/lms/${lan}`);
    router();
}

// ---- LMS Loan Details ----
async function renderLoanDetails(lan) {
    appDiv.innerHTML = `<h2>Loading Account LAN ${lan}...</h2>`;
    const acc = await fetchAPI(`/api/lms/accounts/${lan}/details`);
    appDiv.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>Loan Account #${acc.lan}</h2>
            <a href="/lms" class="btn btn-secondary">← Back to LMS</a>
        </div>
        
        <div class="grid-2">
            <div class="card">
                <h3>Account Summary</h3>
                <p><strong>Status:</strong> <span class="badge badge-${acc.status}">${acc.status}</span></p>
                <p><strong>User ID:</strong> ${acc.userId}</p>
                <p><strong>Amount:</strong> ${acc.amount}</p>
                <p><strong>Rate:</strong> ${acc.rateOfInterest}%</p>
                <p><strong>Time:</strong> ${acc.timePeriod}</p>
                <br>
                ${acc.status !== 'CANCELLED' && acc.status !== 'FORECLOSED' && acc.status !== 'PENDING_CANCELLATION' && acc.status !== 'PENDING_FORECLOSURE' ? `
                    <button class="btn btn-danger" onclick="cancelLoan(${acc.lan})">Cancel Loan</button>
                    <button class="btn btn-danger" style="background:#8b5cf6;" onclick="forecloseLoan(${acc.lan})">Foreclose Loan</button>
                ` : ''}
                ${acc.status === 'PENDING_CANCELLATION' ? `
                    <button class="btn btn-success" onclick="verifyCancel(${acc.lan})">Verify Cancellation</button>
                    <button class="btn btn-info" onclick="activateAccount(${acc.lan})">Activate Account</button>
                ` : ''}
                ${acc.status === 'PENDING_FORECLOSURE' ? `
                    <button class="btn btn-success" onclick="verifyForeclosure(${acc.lan})">Verify Foreclosure</button>
                    <button class="btn btn-info" onclick="activateAccount(${acc.lan})">Activate Account</button>
                ` : ''}
            </div>
            
            <div class="card" id="ledger_box">
                <h3>Ledger (Loading...)</h3>
            </div>
        </div>
        
        <div class="card">
            <h3>Make Payment</h3>
            <div class="grid-4 form-group">
                <input type="number" id="pay_amt" placeholder="Amount">
                <input type="date" id="pay_date" placeholder="Date">
                <button class="btn btn-success" onclick="makePayment(${acc.lan})">Process Payment</button>
            </div>
        </div>
        
        <div class="card" id="snapshot_box"><h3>Current Snapshot (Loading...)</h3></div>
        
        <div class="card" id="charges_box"><h3>LAN Charges (Loading...)</h3></div>
        
        <div class="card" id="schedules_box"><h3>Schedules (Loading...)</h3></div>
        
        <div class="card" id="credits_box"><h3>Loan Credits (Loading...)</h3></div>
        
        <div class="card" id="audits_box"><h3>Statement Audits (Loading...)</h3></div>
    `;
    
    // Load Ledger
    try {
        const due = await fetchAPI(`/api/lms/accounts/${acc.lan}/dues`);
        document.getElementById('ledger_box').innerHTML = `
            <h3>Ledger Totals</h3>
            <p><strong>Total Outstanding:</strong> ${due.totalOutstandingAmount || 0}</p>
            <p><strong>Next Due Date:</strong> ${due.nextDueDate || '-'}</p>
            <p><strong>Next Due Amount:</strong> ${due.nextDueAmount || 0}</p>
            <p><strong>Settled:</strong> ${due.isSettled ? 'Yes' : 'No'}</p>
        `;
    } catch(e) { document.getElementById('ledger_box').innerHTML = `<h3>Ledger</h3><p>Not generated yet.</p>`; }

    // Load Current Snapshot
    try {
        const snap = await fetchAPI(`/api/lms/accounts/${acc.lan}/next-due-status`);
        if (snap.message) {
            document.getElementById('snapshot_box').innerHTML = `<h3>Current Dues Snapshot</h3><p>${snap.message}</p>`;
        } else {
            document.getElementById('snapshot_box').innerHTML = `
                <h3>Current Dues Snapshot (As of Today)</h3>
                <table>
                    <tr>
                        <th>Due Principal</th>
                        <th>Due Interest</th>
                        <th>Due Charges (Penalties)</th>
                        <th>Total Due Amount</th>
                    </tr>
                    <tr>
                        <td>₹${snap.principal}</td>
                        <td>₹${snap.interest}</td>
                        <td>₹${snap.charges}</td>
                        <td style="font-weight:bold; color:var(--danger)">₹${snap.totalDue}</td>
                    </tr>
                </table>
            `;
        }
    } catch (e) {
        document.getElementById('snapshot_box').innerHTML = `<h3>Current Dues Snapshot</h3><p>Could not load snapshot.</p>`;
    }

    // Load Charges (LanCharge)
    try {
        const lanCharge = await fetchAPI(`/api/lms/accounts/${acc.lan}/charges`);
        if (lanCharge) {
            document.getElementById('charges_box').innerHTML = `
                <h3>LAN Charges (Global Penalties & Fees)</h3>
                <table>
                    <tr><th>LAN</th><th>Global Account DPD</th><th>Unpaid Penal Charges</th><th>Unpaid Other Fees</th><th>Last Calculated Date</th></tr>
                    <tr>
                        <td>${lanCharge.lan}</td>
                        <td style="color:var(--danger); font-weight:bold;">${lanCharge.dpd} Days</td>
                        <td style="color:var(--danger); font-weight:bold;">₹${lanCharge.penalCharges}</td>
                        <td style="color:var(--danger); font-weight:bold;">₹${lanCharge.otherFees}</td>
                        <td>${lanCharge.lastCalculatedDate}</td>
                    </tr>
                </table>
            `;
        } else {
            document.getElementById('charges_box').innerHTML = `<h3>LAN Charges</h3><p>No penal charges applied yet.</p>`;
        }
    } catch (e) {
        document.getElementById('charges_box').innerHTML = `<h3>LAN Charges</h3><p>Could not load charges.</p>`;
    }

    // Load Schedules
    try {
        const scheds = await fetchAPI(`/api/settlement/schedules/${acc.lan}`);
        document.getElementById('schedules_box').innerHTML = `
            <h3>Repayment Schedules (Fixed Dues)</h3>
            <table>
                <tr><th>RPS ID</th><th>LAN</th><th>Due Date</th><th>Total Due</th><th>Principal</th><th>Interest</th><th>Status</th></tr>
                ${scheds.map(s => `
                    <tr>
                        <td>${s.rpsId}</td><td>${s.lan}</td><td>${s.dueDate}</td><td>${s.totalDue}</td><td>${s.totalPrincipalDue}</td>
                        <td>${s.totalInterestDue}</td>
                        <td><span class="badge badge-${s.status}">${s.status}</span></td>
                    </tr>
                `).join('')}
            </table>
        `;
    } catch(e) {}

    // Load Credits
    try {
        const credits = await fetchAPI(`/api/settlement/credits/${acc.lan}`);
        if(credits && credits.length > 0) {
            document.getElementById('credits_box').innerHTML = `
                <h3>Loan Credits (Payments)</h3>
                <table>
                    <tr><th>Cred ID</th><th>LAN</th><th>Date</th><th>Requested Amt</th><th>Derived Amt</th><th>Principal Derived</th><th>Interest Derived</th><th>Charges Derived</th><th>Status</th><th>Actions</th></tr>
                    ${credits.map(c => `
                        <tr>
                            <td>${c.credId}</td><td>${c.lan}</td><td>${c.dateOfCredit}</td><td>${c.amtCredited}</td>
                            <td>${c.totalPrincipleDerived + c.totalInterestDerived + c.totalChargesDerived}</td><td>${c.totalPrincipleDerived}</td>
                            <td>${c.totalInterestDerived}</td><td>${c.totalChargesDerived}</td>
                            <td><span class="badge badge-${c.status}">${c.status}</span></td>
                            <td>
                                ${c.status === 'PENDING_LENDER_VERIFICATION' || c.status === 'PENDING' ? `
                                    <button class="btn btn-success" onclick="verifyPayment(${c.credId}, ${acc.lan})">Verify</button>
                                ` : ''}
                            </td>
                        </tr>
                    `).join('')}
                </table>
            `;
        } else {
            document.getElementById('credits_box').innerHTML = `<h3>Loan Credits</h3><p>No payments made yet.</p>`;
        }
    } catch(e) {}

    // Load Audits
    try {
        const audits = await fetchAPI(`/api/settlement/audits/${acc.lan}`);
        if(audits && audits.length > 0) {
            document.getElementById('audits_box').innerHTML = `
                <h3>Statement Audits (Distributions)</h3>
                <table>
                    <tr><th>Audit ID</th><th>Cred ID</th><th>RPS ID</th><th>LAN</th><th>Due Date</th><th>Due (Month)</th><th>Due (Prev)</th><th>Due (Charges)</th><th>Total Due</th><th>Credit Date</th><th>Amt Derived</th><th>Prin Derived</th><th>Int Derived</th><th>Char Derived</th><th>Settled</th><th>Status</th></tr>
                    ${audits.map(a => `
                        <tr>
                            <td>${a.settId}</td><td>${a.credId}</td><td>${a.rpsId}</td><td>${a.lan}</td>
                            <td>${a.dueDate}</td><td>${a.dueForThisMonth}</td><td>${a.dueFromPreviousMonths}</td><td>${a.chargesDue}</td><td>${a.totalDue}</td>
                            <td>${a.dateOfCredit}</td><td>${a.amountDerived}</td><td>${a.principleDerived}</td><td>${a.interestDerived}</td><td>${a.chargesDerived}</td>
                            <td>${a.isSettled ? 'Yes' : 'No'}</td>
                            <td><span class="badge badge-${a.status}">${a.status}</span></td>
                        </tr>
                    `).join('')}
                </table>
            `;
        } else {
            document.getElementById('audits_box').innerHTML = `<h3>Statement Audits</h3><p>No distribution audits yet.</p>`;
        }
    } catch(e) {}
}

window.cancelLoan = async function(lan) {
    const cancelDate = prompt('Enter Date of Cancellation (YYYY-MM-DD):', new Date().toISOString().split('T')[0]);
    if(cancelDate) {
        await fetchAPI(`/api/lms/${lan}/cancel?dateOfCancellation=${cancelDate}`, 'POST');
        showToast('Cancellation Requested');
        renderLoanDetails(lan);
    }
}
window.verifyCancel = async function(lan) {
    if(confirm('Verify this cancellation? (Ensure the cancellation fee has been paid and verified)')) {
        try {
            await fetchAPI(`/api/lms/${lan}/verify-cancellation`, 'PUT');
            showToast('Cancellation Verified and Finalized');
            renderLoanDetails(lan);
        } catch(e) {
            alert(e);
        }
    }
}
window.forecloseLoan = async function(lan) {
    if(confirm('Foreclose this loan?')) { await fetchAPI(`/api/lms/${lan}/foreclose`, 'POST'); showToast('Foreclosed'); renderLoanDetails(lan); }
}

window.verifyForeclosure = async function(lan) {
    if(confirm('Verify this foreclosure? (Ensure the foreclosure amount has been completely paid and verified)')) {
        try {
            await fetchAPI(`/api/lms/${lan}/verify-foreclosure`, 'PUT');
            showToast('Foreclosure Verified and Finalized');
            renderLoanDetails(lan);
        } catch(e) {
            alert(e);
        }
    }
}

window.activateAccount = async function(lan) {
    if(confirm('Are you sure you want to revert this request and restore the original EMI schedules?')) {
        try {
            await fetchAPI(`/api/lms/accounts/${lan}/activate`, 'PUT');
            showToast('Account Activated successfully! Original schedules restored.');
            renderLoanDetails(lan);
        } catch(e) {
            alert("Failed to activate account: " + e);
        }
    }
}

window.verifyPayment = async function(credId, lan) {
    if(confirm('Verify this payment? (Lender action)')) {
        await fetchAPI(`/api/settlement/credit/${credId}/verify`, 'PUT');
        showToast('Payment Verified');
        renderLoanDetails(lan);
    }
}
window.makePayment = async function(lan) {
    const amt = document.getElementById('pay_amt').value;
    const date = document.getElementById('pay_date').value;
    if(!amt) { alert('Enter amount'); return; }
    await fetchAPI(`/api/settlement/credit`, 'POST', { lan: lan, amount: amt, dateOfCredit: date || undefined });
    showToast('Payment processed!');
    renderLoanDetails(lan);
}

// Initialize
router();
