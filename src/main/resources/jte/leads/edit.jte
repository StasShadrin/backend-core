@import ru.mentee.power.crm.model.Lead
@param Lead lead

@template.layout.main(content = @`
    <div class="max-w-md mx-auto mt-8">
        <h1 class="text-2xl font-bold mb-6">Редактирование лида</h1>
        <form action="/leads/${lead.id().toString()}" method="post" class="space-y-4">
            <!-- Скрытое поле с ID (обязательно!) -->
            <input type="hidden" name="id" value="${String.valueOf(lead.id())}">

            <!-- Email -->
            <div>
                <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                        type="email"
                        id="email"
                        name="email"
                        value="${lead.email()}"
                        required
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                />
            </div>

            <!-- Phone -->
            <div>
                <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input
                        type="tel"
                        id="phone"
                        name="phone"
                        value="${lead.phone()}"
                        required
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                />
            </div>

            <!-- Company -->
            <div>
                <label for="company" class="block text-sm font-medium text-gray-700 mb-1">Company</label>
                <input
                        type="text"
                        id="company"
                        name="company"
                        value="${lead.company()}"
                        required
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                />
            </div>

            <!-- Status -->
            <div>
                <label for="status" class="block text-sm font-medium text-gray-700 mb-1">Статус</label>
                <select
                        id="status"
                        name="status"
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                >
                    <option value="NEW">Новый</option>
                    <option value="CONTACTED">Связались</option>
                    <option value="QUALIFIED">Квалифицирован</option>
                    <option value="LOST">Потерян</option>
                </select>
            </div>

            <!-- Submit Button -->
            <button
                    type="submit"
                    class="w-full bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 focus:ring-2 focus:ring-blue-500">
                Сохранить изменения
            </button>
            <!-- Cancel Link -->
            <a
                    href="/leads"
                    class="block text-center text-sm text-gray-600 hover:text-gray-900"
            >
                Отмена
            </a>
        </form>
    </div>
`)