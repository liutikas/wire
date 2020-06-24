// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: person.proto
import Foundation
import Wire

public struct Person : Equatable, Proto2Codable, Codable {

    public var name: String
    public var id: Int32
    public var email: String?
    public var phone: [PhoneNumber]
    public var aliases: [String]
    public var unknownFields: Data = .init()

    public init(
        name: String,
        id: Int32,
        email: String? = nil,
        phone: [PhoneNumber] = [],
        aliases: [String] = []
    ) {
        self.name = name
        self.id = id
        self.email = email
        self.phone = phone
        self.aliases = aliases
    }

    public init(from reader: ProtoReader) throws {
        var name: String? = nil
        var id: Int32? = nil
        var email: String? = nil
        var phone: [PhoneNumber] = []
        var aliases: [String] = []

        let unknownFields = try reader.forEachTag { tag in
            switch tag {
                case 1: name = try reader.decode(String.self)
                case 2: id = try reader.decode(Int32.self)
                case 3: email = try reader.decode(String.self)
                case 4: try reader.decode(into: &phone)
                case 5: try reader.decode(into: &aliases)
                default: try reader.readUnknownField(tag: tag)
            }
        }

        self.name = try Person.checkIfMissing(name, "name")
        self.id = try Person.checkIfMissing(id, "id")
        self.email = email
        self.phone = try Person.checkIfMissing(phone, "phone")
        self.aliases = try Person.checkIfMissing(aliases, "aliases")
        self.unknownFields = unknownFields
    }

    public func encode(to writer: ProtoWriter) throws {
        try writer.encode(tag: 1, value: name)
        try writer.encode(tag: 2, value: id)
        try writer.encode(tag: 3, value: email)
        try writer.encode(tag: 4, value: phone)
        try writer.encode(tag: 5, value: aliases)
        try writer.writeUnknownFields(unknownFields)
    }

    private enum CodingKeys : String, CodingKey {

        case name
        case id
        case email
        case phone
        case aliases

    }

    public enum PhoneType : UInt32, CaseIterable, Codable {

        case MOBILE = 0
        case HOME = 1
        case WORK = 2

    }

    public struct PhoneNumber : Equatable, Proto2Codable, Codable {

        public var number: String
        public var type: PhoneType?
        public var unknownFields: Data = .init()

        public init(number: String, type: PhoneType? = nil) {
            self.number = number
            self.type = type
        }

        public init(from reader: ProtoReader) throws {
            var number: String? = nil
            var type: PhoneType? = nil

            let unknownFields = try reader.forEachTag { tag in
                switch tag {
                    case 1: number = try reader.decode(String.self)
                    case 2: type = try reader.decode(PhoneType.self)
                    default: try reader.readUnknownField(tag: tag)
                }
            }

            self.number = try PhoneNumber.checkIfMissing(number, "number")
            self.type = type
            self.unknownFields = unknownFields
        }

        public func encode(to writer: ProtoWriter) throws {
            try writer.encode(tag: 1, value: number)
            try writer.encode(tag: 2, value: type)
            try writer.writeUnknownFields(unknownFields)
        }

        private enum CodingKeys : String, CodingKey {

            case number
            case type

        }

    }

}
